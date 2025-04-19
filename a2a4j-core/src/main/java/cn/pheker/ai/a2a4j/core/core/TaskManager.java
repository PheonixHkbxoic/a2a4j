package cn.pheker.ai.a2a4j.core.core;

import cn.pheker.ai.a2a4j.core.spec.entity.UpdateEvent;
import cn.pheker.ai.a2a4j.core.spec.message.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:18
 * @desc
 */
public interface TaskManager {

    GetTaskResponse onGetTask(GetTaskRequest request);

    SendTaskResponse onSendTask(SendTaskRequest request);

    Mono<JsonRpcResponse> onSendTaskSubscribe(SendTaskStreamingRequest request);

    CancelTaskResponse onCancelTask(CancelTaskRequest request);

    GetTaskPushNotificationResponse onGetTaskPushNotification(GetTaskPushNotificationRequest request);

    SetTaskPushNotificationResponse onSetTaskPushNotification(SetTaskPushNotificationRequest request);

    Mono<JsonRpcResponse> onResubscribeTask(TaskResubscriptionRequest request);

    Flux<UpdateEvent> dequeueEvent(String taskId);

    Mono<Void> closeGracefully();
}
