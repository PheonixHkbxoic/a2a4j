package cn.pheker.ai.core;

import cn.pheker.ai.spec.Nullable;
import cn.pheker.ai.spec.ValueError;
import cn.pheker.ai.spec.entity.*;
import cn.pheker.ai.spec.error.InternalError;
import cn.pheker.ai.spec.error.TaskNotCancelableError;
import cn.pheker.ai.spec.error.TaskNotFoundError;
import cn.pheker.ai.spec.message.*;
import cn.pheker.ai.util.Util;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 23:27
 * @desc
 */
@Slf4j
public abstract class InMemoryTaskManager implements TaskManager {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, Task> tasks = new HashMap<>();
    private final Map<String, PushNotificationConfig> pushNotificationConfigs = new HashMap<>();

    private final ConcurrentMap<String, LinkedBlockingQueue<UpdateEvent>> sseEventQueueMap = new ConcurrentHashMap<>();


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
        queue.offer(updateEvent);
    }

    @Override
    public Mono<Void> dequeueEvent(String taskId, Consumer<UpdateEvent> consumer) {
        LinkedBlockingQueue<UpdateEvent> queue = sseEventQueueMap.get(taskId);
        log.debug("dequeueEvent taskId: {}, restEventSize: {}", taskId, getRestEventSize(taskId));
        Flux.<UpdateEvent>create(sink -> {
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
                throw new RuntimeException(e);
            }
        }).publishOn(Schedulers.single()).doOnError(e -> {
            log.error("dequeueEvent taskId: {}, error: {}", taskId, e.getMessage());
            sseEventQueueMap.remove(taskId);
        }).doOnComplete(() -> {
            sseEventQueueMap.remove(taskId);
        }).subscribe(consumer);
        return Mono.empty();
    }

    @Override
    public long getRestEventSize(String taskId) {
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

        Task taskResult = this.appendTaskHistory(task, request.getParams().getHistoryLength());

        return new GetTaskResponse(request.getId(), taskResult);
    }

//    @Override
//    public SendTaskResponse onSendTask(SendTaskRequest request) {
//        return new SendTaskResponse(null);
//    }

//    @Override
//    public SendTaskStreamingResponse onSendTaskSubscribe(SendTaskStreamingRequest request) {
//        return null;
//    }

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
        lock.lock();
        try {
            Task task = tasks.get(taskId);
            if (task == null) {
                return new GetTaskPushNotificationResponse(taskId, new TaskNotFoundError());
            }
            TaskPushNotificationConfig taskPushNotificationConfig = TaskPushNotificationConfig.builder().id(taskId).pushNotificationConfig(pushNotificationConfigs.get(taskId)).build();
            return new GetTaskPushNotificationResponse(request.getId(), taskPushNotificationConfig);
        } catch (Exception e) {
            log.error("Getting task push notification exception: {}", e.getMessage());
            return new GetTaskPushNotificationResponse(request.getId(), new InternalError("An error occurred while getting push notification config"));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SetTaskPushNotificationResponse onSetTaskPushNotification(SetTaskPushNotificationRequest request) {
        String taskId = request.getParams().getId();
        log.info("Setting task push notification: {}", taskId);
        lock.lock();
        try {
            Task task = tasks.get(request.getParams().getId());
            if (task == null) {
                return new SetTaskPushNotificationResponse(taskId, new TaskNotFoundError());
            }
            pushNotificationConfigs.put(taskId, request.getParams().getPushNotificationConfig());
        } catch (Exception e) {
            log.error("Setting task push notification exception: {}", e.getMessage());
            return new SetTaskPushNotificationResponse(taskId, new InternalError("An error occurred while setting push notification config"));
        } finally {
            lock.unlock();
        }

        return new SetTaskPushNotificationResponse(taskId, request.getParams());
    }

//    @Override
//    public SendTaskStreamingResponse onResubscribeTask(TaskResubscriptionRequest request) {
//        return null;
//    }


    protected Task upsertTask(TaskSendParams taskSendParams) {
        log.info("Upserting task: {}", taskSendParams.getId());

        Task task = this.tasks.get(taskSendParams.getId());
        if (task == null) {
            task = Task.builder().id(taskSendParams.getId()).sessionId(taskSendParams.getSessionId()).status(new TaskStatus(TaskState.SUBMITTED)).history(new ArrayList<>()).build();
            this.tasks.put(taskSendParams.getId(), task);
        }

        if (taskSendParams.getMessage() != null) {
            task.getHistory().add(taskSendParams.getMessage());
        }
        return task;
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
                    task.setArtifacts(new ArrayList<Artifact>());
                }
                task.getArtifacts().addAll(artifacts);
            }
            return task;
        } finally {
            lock.unlock();
        }
    }

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

}
