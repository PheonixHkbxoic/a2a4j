package io.github.pheonixhkbxoic.a2a4j.core.core;

import io.github.pheonixhkbxoic.a2a4j.core.spec.ValueError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.PushNotificationConfig;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Task;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/28 23:21
 * @desc
 */
public class InMemoryTaskStore implements TaskStore {
    protected final Map<String, Task> tasks = new HashMap<>();
    protected final Map<String, List<String>> sessionTaskIds = new HashMap<>();
    protected final Map<String, PushNotificationConfig> pushNotificationInfos = new HashMap<>();
    protected ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public void insert(Task task) {
        ReentrantReadWriteLock.WriteLock w = rwLock.writeLock();
        w.lock();
        try {
            String taskId = task.getId();
            Task exist = tasks.get(taskId);
            if (exist == null) {
                tasks.put(taskId, task);
                List<String> taskIds = sessionTaskIds.computeIfAbsent(task.getSessionId(), k -> new LinkedList<>());
                taskIds.add(taskId);
            }
        } finally {
            w.unlock();
        }
    }

    @Override
    public void delete(String taskId) {
        ReentrantReadWriteLock.WriteLock w = rwLock.writeLock();
        w.lock();
        try {
            Task exist = tasks.remove(taskId);
            if (exist != null) {
                List<String> taskIds = sessionTaskIds.get(exist.getSessionId());
                taskIds.remove(taskId);
            }
        } finally {
            w.unlock();
        }
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        ReentrantReadWriteLock.WriteLock w = rwLock.writeLock();
        w.lock();
        try {
            sessionTaskIds.getOrDefault(sessionId, List.of()).forEach(tasks::remove);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void update(Task task) {
        ReentrantReadWriteLock.WriteLock w = rwLock.writeLock();
        w.lock();
        try {
            String taskId = task.getId();
            Task exist = tasks.put(taskId, task);
            if (exist == null) {
                List<String> taskIds = sessionTaskIds.computeIfAbsent(task.getSessionId(), k -> new LinkedList<>());
                taskIds.add(taskId);
            }
        } finally {
            w.unlock();
        }
    }

    @Override
    public Task query(String taskId) {
        ReentrantReadWriteLock.ReadLock r = rwLock.readLock();
        r.lock();
        try {
            return tasks.get(taskId);
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Task> queryBySessionId(String sessionId) {
        ReentrantReadWriteLock.ReadLock r = rwLock.readLock();
        r.lock();
        try {
            return sessionTaskIds.getOrDefault(sessionId, List.of()).stream()
                    .map(tasks::get)
                    .filter(Objects::nonNull)
                    .toList();
        } finally {
            r.unlock();
        }
    }


    @Override
    public boolean hasPushNotificationInfo(String taskId) {
        return pushNotificationInfos.get(taskId) != null;
    }

    @Override
    public PushNotificationConfig getPushNotificationInfo(String taskId) {
        ReentrantReadWriteLock.ReadLock r = rwLock.readLock();
        r.lock();
        try {
            Task task = this.query(taskId);
            if (task == null) {
                throw new ValueError("Task not found for " + taskId);
            }
            return this.pushNotificationInfos.get(taskId);
        } finally {
            r.unlock();
        }
    }

    @Override
    public void setPushNotificationInfo(String taskId, PushNotificationConfig info) {
        ReentrantReadWriteLock.WriteLock w = rwLock.writeLock();
        w.lock();
        try {
            Task task = this.query(taskId);
            if (task == null) {
                throw new ValueError("Task not found for " + taskId);
            }
            this.pushNotificationInfos.put(taskId, info);
        } finally {
            w.unlock();
        }
    }
}
