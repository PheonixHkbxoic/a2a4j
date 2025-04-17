package cn.pheker.ai.test.server;

import cn.pheker.ai.core.InMemoryTaskManager;
import cn.pheker.ai.spec.ValueError;
import cn.pheker.ai.spec.entity.*;
import cn.pheker.ai.spec.error.InternalError;
import cn.pheker.ai.spec.error.InvalidParamsError;
import cn.pheker.ai.spec.message.*;
import cn.pheker.ai.util.Util;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/14 15:33
 * @desc
 */
@Slf4j
public class EchoTaskManager extends InMemoryTaskManager {
    // wire agent
    private final EchoAgent agent;
    // agent support modes
    private final List<String> supportModes = Arrays.asList("text", "file", "data");

    public EchoTaskManager(EchoAgent agent) {
        this.agent = agent;
    }

    @Override
    public SendTaskResponse onSendTask(SendTaskRequest request) {
        log.info("onSendTask request: {}", request);
        TaskSendParams ps = request.getParams();
        // 1. check
        JsonRpcResponse<Object> error = this.validRequest(request);
        if (error != null) {
            return new SendTaskResponse(request.getId(), error.getError());
        }
        // TODO check pushNotification

        // 2. save and notification
        this.upsertTask(ps);
        this.updateStore(ps.getId(), new TaskStatus(TaskState.WORKING), null);
        // TODO send task notification

        // 3. agent invoke
        log.info("sessionId: {}", ps.getSessionId());
        List<Artifact> artifacts = this.agentInvoke(ps).block();

        // 4. save and notification
        Task task = this.updateStore(ps.getId(), new TaskStatus(TaskState.COMPLETED), artifacts);
        // TODO send task notification

        Task taskSnapshot = this.appendTaskHistory(task, 3);
        return new SendTaskResponse(taskSnapshot);
    }

    @Override
    public Mono<JsonRpcResponse> onSendTaskSubscribe(SendTaskStreamingRequest request) {
        log.info("onSendTaskSubscribe request: {}", request);
        TaskSendParams ps = request.getParams();
        String taskId = ps.getId();

        try {
            // 1. check
            JsonRpcResponse<Object> error = this.validRequest(request);
            if (error != null) {
                throw new ValueError(error.getError().getMessage());
            }
            // check pushNotification

            // save
            this.upsertTask(ps);
            this.updateStore(taskId, new TaskStatus(TaskState.WORKING), null);
            // TODO send task notification


            this.initEventQueue(taskId, false);
            // start thread to hand agent task
            this.startAgentTaskThread(taskId);

        } catch (Exception e) {
            return Mono.just(new JsonRpcResponse<>(request.getId(), new InternalError(e.getMessage())));
        }
        return Mono.empty();
    }

    private void startAgentTaskThread(String taskId) {
        // simulate agent token stream and enqueue
        Flux.range(1, 10).subscribeOn(Schedulers.single()).subscribe(i -> {
            List<Part> parts = Collections.singletonList(new TextPart("sse message: " + i));
            TaskState state;
            Message message = null;
            Artifact artifact = null;
            boolean finalFlag = false;
            if (i == 10) {
                state = TaskState.COMPLETED;
                finalFlag = true;
                artifact = new Artifact(parts, 0, false);
            } else {
                state = TaskState.WORKING;
                message = new Message(Role.AGENT, parts, null);
            }

            TaskStatus taskStatus = new TaskStatus(state, message);
            Task latestTask = this.updateStore(taskId, taskStatus, Collections.singletonList(artifact));
            // send notification
            // TODO

            // artifact event
            if (artifact != null) {
                TaskArtifactUpdateEvent taskArtifactUpdateEvent = new TaskArtifactUpdateEvent(taskId, artifact);
                this.enqueueEvent(taskId, taskArtifactUpdateEvent);
            }

            // status event
            TaskStatusUpdateEvent taskStatusUpdateEvent = new TaskStatusUpdateEvent(taskId, taskStatus, finalFlag);
            this.enqueueEvent(taskId, taskStatusUpdateEvent);
        });
    }

    @Override
    public Mono<JsonRpcResponse> onResubscribeTask(TaskResubscriptionRequest request) {
        TaskIdParams params = request.getParams();
        try {
            this.initEventQueue(params.getId(), true);
        } catch (Exception e) {
            log.error("Error while reconnecting to SSE stream: {}", e.getMessage());
            return Mono.just(new JsonRpcResponse(request.getId(), new InternalError("An error occurred while reconnecting to stream: " + e.getMessage())));
        }

        return Mono.empty();
    }


    // simulate agent invoke
    private Mono<List<Artifact>> agentInvoke(TaskSendParams ps) {
        List<Part> parts = ps.getMessage().getParts();
        String prompts = parts.stream()
                .filter(p -> p instanceof TextPart)
                .map(p -> ((TextPart) p).getText())
                .collect(Collectors.joining("\n"));

        return this.agent.chat(prompts).map(answer -> {
            Artifact artifact = Artifact.builder()
                    .name("echo")
                    .description("echo request")
                    .append(false)
                    .parts(Collections.singletonList(new TextPart(answer)))
                    .build();
            return Collections.singletonList(artifact);
        });
    }

    private JsonRpcResponse<Object> validRequest(SendTaskRequest request) {
        TaskSendParams ps = request.getParams();
        if (!Util.areModalitiesCompatible(ps.getAcceptedOutputModes(), supportModes)) {
            log.warn("Unsupported output mode. Received: {}, Support: {}",
                    ps.getAcceptedOutputModes(),
                    supportModes);
            return Util.newIncompatibleTypesError(request.getId());
        }

        if (ps.getPushNotification() != null && Util.isEmpty(ps.getPushNotification().getUrl())) {
            log.warn("Push notification URL is missing");
            return new JsonRpcResponse<>(request.getId(), new InvalidParamsError("Push notification URL is missing"));
        }
        return null;
    }

    private JsonRpcResponse<Object> validRequest(SendTaskStreamingRequest request) {
        TaskSendParams ps = request.getParams();
        if (!Util.areModalitiesCompatible(ps.getAcceptedOutputModes(), supportModes)) {
            log.warn("Unsupported output mode. Received: {}, Support: {}",
                    ps.getAcceptedOutputModes(),
                    supportModes);
            return Util.newIncompatibleTypesError(request.getId());
        }

        if (ps.getPushNotification() != null && Util.isEmpty(ps.getPushNotification().getUrl())) {
            log.warn("Push notification URL is missing");
            return new JsonRpcResponse<>(request.getId(), new InvalidParamsError("Push notification URL is missing"));
        }
        return null;
    }
}

