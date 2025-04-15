package cn.pheker.ai.core;

import cn.pheker.ai.spec.entity.UpdateEvent;
import cn.pheker.ai.spec.message.*;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:18
 * @desc
 */
public interface TaskManager {

    GetTaskResponse onGetTask(GetTaskRequest request);

    SendTaskResponse onSendTask(SendTaskRequest request);

    Mono<Void> onSendTaskSubscribe(SendTaskStreamingRequest request);

    CancelTaskResponse onCancelTask(CancelTaskRequest request);

    GetTaskPushNotificationResponse onGetTaskPushNotification(GetTaskPushNotificationRequest request);

    SetTaskPushNotificationResponse onSetTaskPushNotification(SetTaskPushNotificationRequest request);

    Mono<Void> onResubscribeTask(TaskResubscriptionRequest request);

    Mono<Void> dequeueEvent(String taskId, Consumer<UpdateEvent> consumer);

    long getRestEventSize(String taskId);
}
