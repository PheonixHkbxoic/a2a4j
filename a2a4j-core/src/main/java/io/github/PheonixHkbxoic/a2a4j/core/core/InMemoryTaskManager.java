package io.github.PheonixHkbxoic.a2a4j.core.core;

import io.github.PheonixHkbxoic.a2a4j.core.spec.Nullable;
import io.github.PheonixHkbxoic.a2a4j.core.spec.ValueError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.*;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.InternalError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.TaskNotCancelableError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.TaskNotFoundError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.message.*;
import io.github.PheonixHkbxoic.a2a4j.core.util.Util;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author PheonixHkbxoic
 */
@Slf4j
public abstract class InMemoryTaskManager implements TaskManager {
    protected final ReentrantLock lock = new ReentrantLock();
    protected final Map<String, Task> tasks = new HashMap<>();
    protected final Map<String, PushNotificationConfig> pushNotificationInfos = new HashMap<>();
    protected final ConcurrentMap<String, LinkedBlockingQueue<UpdateEvent>> sseEventQueueMap = new ConcurrentHashMap<>();
    protected boolean isClosing;
    protected PushNotificationSenderAuth pushNotificationSenderAuth;


    public InMemoryTaskManager() {
    }

    protected LinkedBlockingQueue<UpdateEvent> initEventQueue(String taskId, boolean resubscribe) {
        LinkedBlockingQueue<UpdateEvent> queue = sseEventQueueMap.get(taskId);
        if (queue == null) {
            if (resubscribe) {
                throw new ValueError("Task not found for resubscription");
            }
            queue = new LinkedBlockingQueue<>();
            sseEventQueueMap.put(taskId, queue);
        }
        return queue;
    }

    protected void enqueueEvent(String taskId, UpdateEvent updateEvent) {
        log.debug("enqueueEvent taskId: {}, updateEvent: {}", taskId, updateEvent);
        LinkedBlockingQueue<UpdateEvent> queue = sseEventQueueMap.get(taskId);
        if (queue == null) {
            throw new RuntimeException("queue not init, taskId: " + taskId);
        }
        if (isClosing) {
            throw new RuntimeException("server is closing");
        }
        queue.offer(updateEvent);
    }

    @Override
    public Flux<UpdateEvent> dequeueEvent(String taskId) {
        LinkedBlockingQueue<UpdateEvent> queue = sseEventQueueMap.get(taskId);
        if (queue == null) {
            return Flux.empty();
        }
        log.debug("dequeueEvent taskId: {}, restEventSize: {}", taskId, getRestEventSize(taskId));
        return Flux.<UpdateEvent>create(sink -> {
                    try {
                        while (true) {
                            //                    log.debug("dequeueEvent taskId: {}, restEventSize: {}", taskId, getRestEventSize(taskId));
                            UpdateEvent event = queue.take();
                            sink.next(event);
                            if (event instanceof TaskStatusUpdateEvent && ((TaskStatusUpdateEvent) event).isFinalFlag()) {
                                sink.complete();
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        sink.error(e);
                    }
                })
                .doOnComplete(() -> {
                    sseEventQueueMap.remove(taskId);
                })
                .doOnError(e -> {
                    log.error("dequeueEvent taskId: {}, restEventSize: {}, error: {}", taskId, getRestEventSize(taskId), e.getMessage(), e);
                });
    }

    protected long getRestEventSize(String taskId) {
        LinkedBlockingQueue<UpdateEvent> queue = sseEventQueueMap.get(taskId);
        if (queue == null) {
            return -1;
        }
        return queue.size();
    }

    @Override
    public GetTaskResponse onGetTask(GetTaskRequest request) {
        log.info("Getting task {}", request.getParams().getId());

        Task task = this.tasks.get(request.getParams().getId());
        if (task == null) {
            return new GetTaskResponse(request.getId(), new TaskNotFoundError());
        }

        Task taskSnapshot = this.appendTaskHistory(task, request.getParams().getHistoryLength());
        return new GetTaskResponse(request.getId(), taskSnapshot);
    }


    @Override
    public CancelTaskResponse onCancelTask(CancelTaskRequest request) {
        log.info("Cancelling task: {}", request.getParams().getId());
        Task task = tasks.get(request.getParams().getId());
        if (task == null) {
            return new CancelTaskResponse(request.getId(), new TaskNotFoundError());
        }
        return new CancelTaskResponse(request.getId(), new TaskNotCancelableError());
    }

    @Override
    public GetTaskPushNotificationResponse onGetTaskPushNotification(GetTaskPushNotificationRequest request) {
        String taskId = request.getParams().getId();
        log.info("Getting task push notification: {}", taskId);
        try {
            PushNotificationConfig pushNotificationInfo = this.getPushNotificationInfo(taskId);
            TaskPushNotificationConfig taskPushNotificationConfig = TaskPushNotificationConfig.builder()
                    .id(taskId)
                    .pushNotificationConfig(pushNotificationInfo)
                    .build();
            return new GetTaskPushNotificationResponse(request.getId(), taskPushNotificationConfig);
        } catch (Exception e) {
            log.error("Getting task push notification exception: {}", e.getMessage());
            return new GetTaskPushNotificationResponse(request.getId(), new InternalError("An error occurred while getting push notification config"));
        }
    }

    @Override
    public SetTaskPushNotificationResponse onSetTaskPushNotification(SetTaskPushNotificationRequest request) {
        String taskId = request.getParams().getId();
        log.info("Setting task push notification: {}", taskId);
        try {
            this.setPushNotificationInfo(taskId, request.getParams().getPushNotificationConfig());
        } catch (Exception e) {
            log.error("Setting task push notification exception: {}", e.getMessage());
            return new SetTaskPushNotificationResponse(taskId, new InternalError("An error occurred while setting push notification config"));
        }

        return new SetTaskPushNotificationResponse(taskId, request.getParams());
    }

    protected boolean hasPushNotificationInfo(String taskId) {
        return pushNotificationInfos.get(taskId) != null;
    }

    protected PushNotificationConfig getPushNotificationInfo(String taskId) {
        lock.lock();
        try {
            Task task = this.tasks.get(taskId);
            if (task == null) {
                throw new ValueError("Task not found for " + taskId);
            }
            return this.pushNotificationInfos.get(taskId);
        } finally {
            lock.unlock();
        }
    }

    protected void setPushNotificationInfo(String taskId, PushNotificationConfig info) {
        lock.lock();
        try {
            Task task = this.tasks.get(taskId);
            if (task == null) {
                throw new ValueError("Task not found for " + taskId);
            }
            this.pushNotificationInfos.put(taskId, info);
        } finally {
            lock.unlock();
        }
    }

    protected boolean verifyPushNotificationInfo(String taskId, PushNotificationConfig info) {
        return PushNotificationSenderAuth.verifyPushNotificationUrl(info.getUrl());
    }

    protected void sendTaskNotification(Task task) {
        if (this.pushNotificationSenderAuth == null) {
            log.warn("PushNotificationSenderAuth not be set");
            return;
        }
        if (!hasPushNotificationInfo(task.getId())) {
            log.info("No push notification info found for task: {}", task.getId());
            return;
        }
        PushNotificationConfig pushNotificationInfo = this.getPushNotificationInfo(task.getId());
        log.info("Notifying for task: {}, {}", task.getId(), task.getStatus().getState().getState());
        this.pushNotificationSenderAuth.sendPushNotification(pushNotificationInfo.getUrl(), task);
    }

    protected Task upsertTask(TaskSendParams taskSendParams) {
        log.info("Upserting task: {}", taskSendParams.getId());

        lock.lock();
        try {
            Task task = this.tasks.get(taskSendParams.getId());
            if (task == null) {
                task = Task.builder().id(taskSendParams.getId()).sessionId(taskSendParams.getSessionId()).status(new TaskStatus(TaskState.SUBMITTED)).history(new ArrayList<>()).build();
                this.tasks.put(taskSendParams.getId(), task);
            }

            if (taskSendParams.getMessage() != null) {
                task.getHistory().add(taskSendParams.getMessage());
            }
            this.setPushNotificationInfo(task.getId(), taskSendParams.getPushNotification());
            return task;
        } finally {
            lock.unlock();
        }
    }

    protected Task updateStore(String taskId, TaskStatus status, List<Artifact> artifacts) {
//        log.info("update Store: {}", taskId);
        lock.lock();
        try {
            Task task = tasks.get(taskId);
            if (task == null) {
                log.error("Task not found for updating the task: {}", taskId);
                throw new ValueError("Task not found: " + taskId);
            }
            task.setStatus(status);
            if (status.getMessage() != null) {
                task.getHistory().add(status.getMessage());
            }
            if (artifacts != null && !artifacts.isEmpty()) {
                if (task.getArtifacts() == null) {
                    task.setArtifacts(new ArrayList<>());
                }
                task.getArtifacts().addAll(artifacts);
            }
            return task;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取task快照，并返回最多historyLength条历史消息
     *
     * @param task
     * @param historyLength
     * @return
     */
    protected Task appendTaskHistory(Task task, @Nullable Integer historyLength) {
        Task copy = Util.deepCopyJson(task, Task.class);
        assert copy != null;
        List<Message> hs = copy.getHistory();
        if (hs != null && !hs.isEmpty() && historyLength != null && historyLength > 0) {
            List<Message> lastList = hs.subList(historyLength > hs.size() ? 0 : hs.size() - historyLength, hs.size());
            copy.setHistory(new ArrayList<>(lastList));
        } else {
            copy.setHistory(new ArrayList<>());
        }
        return copy;
    }

    @Override
    public Mono<Void> closeGracefully() {
        return Mono.fromRunnable(() -> Flux.fromStream(sseEventQueueMap.keySet().stream()).doFirst(() -> isClosing = true).subscribe(sseEventQueueMap::remove));
    }
}
