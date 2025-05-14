package io.github.pheonixhkbxoic.a2a4j.core.core;

import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.PushNotificationConfig;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Task;

import java.util.List;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/28 23:14
 * @desc
 */
public interface TaskStore {


    void insert(Task task);

    void delete(String taskId);

    void deleteBySessionId(String sessionId);

    void update(Task task);

    Task query(String taskId);

    List<Task> queryBySessionId(String sessionId);


    boolean hasPushNotificationInfo(String taskId);

    PushNotificationConfig getPushNotificationInfo(String taskId);

    void setPushNotificationInfo(String taskId, PushNotificationConfig info);
}
