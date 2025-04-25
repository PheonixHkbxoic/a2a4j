package io.github.pheonixhkbxoic.a2a4j.core.core;

import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.UpdateEvent;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 */
public interface TaskManager {

    Mono<GetTaskResponse> onGetTask(GetTaskRequest request);

    Mono<SendTaskResponse> onSendTask(SendTaskRequest request);

    Mono<? extends JsonRpcResponse<?>> onSendTaskSubscribe(SendTaskStreamingRequest request);

    Mono<CancelTaskResponse> onCancelTask(CancelTaskRequest request);

    Mono<GetTaskPushNotificationResponse> onGetTaskPushNotification(GetTaskPushNotificationRequest request);

    Mono<SetTaskPushNotificationResponse> onSetTaskPushNotification(SetTaskPushNotificationRequest request);

    Mono<? extends JsonRpcResponse<?>> onResubscribeTask(TaskResubscriptionRequest request);

    Flux<UpdateEvent> dequeueEvent(String taskId);

    Mono<Void> closeGracefully();
}
