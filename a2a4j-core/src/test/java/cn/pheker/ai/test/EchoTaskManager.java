package cn.pheker.ai.test;

import cn.pheker.ai.core.InMemoryTaskManager;
import cn.pheker.ai.spec.ValueError;
import cn.pheker.ai.spec.entity.*;
import cn.pheker.ai.spec.error.InvalidParamsError;
import cn.pheker.ai.spec.message.*;
import cn.pheker.ai.util.Util;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        log.info("request: {}", request);
        TaskSendParams ps = request.getParams();
        // 1. check
        JsonRpcResponse<Object> error = this.validRequest(request);
        if (error != null) {
            return new SendTaskResponse(request.getId(), error.getError());
        }
        // check pushNotification

        // save
        this.upsertTask(ps);
        this.updateStore(ps.getId(), new TaskStatus(TaskState.WORKING), null);

        // send task notification

        // 2. agent invoke
        log.info("sessionId: {}", ps.getSessionId());
        List<Artifact> artifacts = this.agentInvoke(ps).block();
        Task task = this.updateStore(ps.getId(), new TaskStatus(TaskState.COMPLETED), artifacts);

        // handle agent response
        this.appendTaskHistory(task, 3);

        return new SendTaskResponse(task);
    }

    @Override
    public Mono<Void> onSendTaskSubscribe(SendTaskStreamingRequest request) {
        return Mono.fromRunnable(() -> {
            log.info("request: {}", request);
            TaskSendParams ps = request.getParams();
            // 1. check
            JsonRpcResponse<Object> error = this.validRequest(request);
            if (error != null) {
                throw new ValueError(error.getError().getMessage());
            }
            // check pushNotification

            // save
            this.upsertTask(ps);
            Task task = this.updateStore(ps.getId(), new TaskStatus(TaskState.WORKING), null);

            this.initEventQueue(task.getId(), false);

            // simulate agent token stream
            Flux.range(1, 10)
                    .doOnNext(i -> {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .subscribe(i -> {
                        List<Part> parts = Arrays.asList(new TextPart("sse message: " + i));
                        TaskState state;
                        Message message = null;
                        Artifact artifact = null;
                        boolean finalFlag = false;
                        if (i == 10) {
                            state = TaskState.COMPLETED;
                            artifact = new Artifact(parts, 0, false);
                        } else {
                            state = TaskState.WORKING;
                            message = new Message(Role.AGENT, parts, null);
                        }

                        TaskStatus taskStatus = new TaskStatus(state, message);
                        Task latestTask = this.updateStore(task.getId(), taskStatus, Arrays.asList(artifact));
                        // send notification

                        // artifact event
                        if (artifact != null) {
                            TaskArtifactUpdateEvent taskArtifactUpdateEvent = new TaskArtifactUpdateEvent(task.getId(), artifact);
                            this.enqueueEvent(task.getId(), taskArtifactUpdateEvent);
                        }

                        // status event
                        TaskStatusUpdateEvent taskStatusUpdateEvent = new TaskStatusUpdateEvent(task.getId(), taskStatus, finalFlag);
                        this.enqueueEvent(task.getId(), taskStatusUpdateEvent);
                    });
        });
    }

    @Override
    public Mono<Void> onResubscribeTask(TaskResubscriptionRequest request) {
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

