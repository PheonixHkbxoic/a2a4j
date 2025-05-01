package io.github.pheonixhkbxoic.a2a4j.core.core;

import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.InternalError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.InvalidParamsError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.JsonRpcResponse;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskRequest;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskResponse;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskStreamingRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.Objects;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/30 22:53
 * @desc
 */
@Slf4j
public class InMemoryTaskManager extends AbstractTaskManager {
    public InMemoryTaskManager(TaskStore taskStore, PushNotificationSenderAuth pushNotificationSenderAuth, AgentInvoker agentInvoker) {
        super(taskStore, pushNotificationSenderAuth, agentInvoker);
    }

    @Override
    public Mono<SendTaskResponse> onSendTask(SendTaskRequest request) {
        log.info("onSendTask request: {}", request);
        TaskSendParams ps = request.getParams();
        // 1. check
        JsonRpcResponse<Object> error = this.validRequest(request);
        if (error != null) {
            return Mono.just(new SendTaskResponse(request.getId(), error.getError()));
        }
        // 2. check and set pushNotification
        if (ps.getPushNotification() != null) {
            boolean verified = this.verifyPushNotificationInfo(ps.getPushNotification());
            if (!verified) {
                return Mono.just(new SendTaskResponse(request.getId(), new InvalidParamsError("Push notification URL is invalid")));
            }
        }

        // 3. save
        this.upsertTask(ps);
        Task taskWorking = this.updateStore(ps.getId(), new TaskStatus(TaskState.WORKING), null);
        this.sendTaskNotification(taskWorking);

        // 4. agent invoke
        return agentInvoker.invoke(request)
                .map(artifacts -> {
                    // 5. save and notification
                    Task taskCompleted = this.updateStore(ps.getId(), new TaskStatus(TaskState.COMPLETED), artifacts);
                    this.sendTaskNotification(taskCompleted);

                    // 6. return task snapshot
                    int historyLength = ps.getHistoryLength() == null || ps.getHistoryLength() < 0 ? agentInvoker.returnTaskHistoryNum() : ps.getHistoryLength();
                    Task taskSnapshot = this.appendTaskHistory(taskCompleted, historyLength);
                    return new SendTaskResponse(taskSnapshot);
                });
    }

    @Override
    public Mono<? extends JsonRpcResponse<?>> onSendTaskSubscribe(SendTaskStreamingRequest request) {
        return Mono.fromCallable(() -> {
            log.debug("onSendTaskSubscribe request: {}", request);
            TaskSendParams ps = request.getParams();
            String taskId = ps.getId();

            try {
                // 1. check
                JsonRpcResponse<Object> error = this.validRequest(request);
                if (error != null) {
                    return new SendTaskResponse(request.getId(), error.getError());
                }
                // 2. check and set pushNotification
                if (ps.getPushNotification() != null) {
                    boolean verified = this.verifyPushNotificationInfo(ps.getPushNotification());
                    if (!verified) {
                        return new SendTaskResponse(request.getId(), new InvalidParamsError("Push notification URL is invalid"));
                    }
                }

                // 3. save
                this.upsertTask(ps);
                Task taskWorking = this.updateStore(taskId, new TaskStatus(TaskState.WORKING), null);
                this.sendTaskNotification(taskWorking);

                // 4. init event queue
                this.initEventQueue(taskId, false);

                // 5. subscribe agent stream data
                agentInvoker.invokeStream(request)
                        .filter(Objects::nonNull)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(streamData -> {
                            Message message = streamData.getMessage();
                            Artifact artifact = streamData.getArtifact();
                            TaskStatus taskStatus = new TaskStatus(streamData.getState(), message);
                            Task latestTask = this.updateStore(taskId, taskStatus, Collections.singletonList(artifact));
                            // 6. send notification
                            this.sendTaskNotification(latestTask);

                            // 7. artifact event
                            if (artifact != null) {
                                TaskArtifactUpdateEvent taskArtifactUpdateEvent = new TaskArtifactUpdateEvent(taskId, artifact);
                                this.enqueueEvent(taskId, taskArtifactUpdateEvent);
                            }

                            // 6. status event
                            TaskStatusUpdateEvent taskStatusUpdateEvent = new TaskStatusUpdateEvent(taskId, taskStatus, streamData.isEndStream());
                            this.enqueueEvent(taskId, taskStatusUpdateEvent);
                        });
            } catch (Exception e) {
                log.error("Error in SSE stream, requestId: {}, error: {}", request.getId(), e.getMessage(), e);
                return new JsonRpcResponse<>(request.getId(), new InternalError("An error occurred while streaming the response"));
            }
            return null;
        });
    }
}
