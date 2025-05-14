package io.github.pheonixhkbxoic.a2a4j.storage.redis;

import io.github.pheonixhkbxoic.a2a4j.core.core.TaskStore;
import io.github.pheonixhkbxoic.a2a4j.core.spec.ValueError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.PushNotificationConfig;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Task;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author PheonixHkbxoic
 * @date 2025/5/14 15:30
 * @desc
 */
@Slf4j
@SuppressWarnings("unchecked")
public class RedisTaskStore implements TaskStore {
    public static String PREFIX = "a2a4j:";
    public static String PREFIX_TASKS = PREFIX + "tasks:";
    public static String PREFIX_SESSIONS = PREFIX + "sessions:";
    public static String PREFIX_PUSH_NOTIFICATION_INFOS = PREFIX + "pushNotificationInfos:";
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTaskStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void insert(Task task) {
        redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().set(PREFIX_TASKS + task.getId(), task);
                operations.opsForSet().add(PREFIX_SESSIONS + task.getSessionId(), task.getId());
                return operations.exec();
            }
        });
    }

    @Override
    public void delete(String taskId) {
        Task task = ((Task) redisTemplate.opsForValue().get(PREFIX_TASKS + taskId));
        if (task != null) {
            redisTemplate.execute(new SessionCallback<>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.delete(PREFIX_TASKS + taskId);
                    operations.opsForSet().remove(PREFIX_SESSIONS + task.getSessionId(), taskId);
                    return operations.exec();
                }
            });
        }
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        Set<Object> taskIdList = redisTemplate.opsForSet().members(PREFIX_SESSIONS + sessionId);
        if (!Util.isEmpty(taskIdList)) {
            List<String> keys = taskIdList.stream().map(taskId -> PREFIX_TASKS + taskId).toList();
            redisTemplate.execute(new SessionCallback<>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForValue().getOperations().delete(keys);
                    operations.delete(PREFIX_SESSIONS + sessionId);
                    return operations.exec();
                }
            });
        }
    }

    @Override
    public void update(Task task) {
        redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().set(PREFIX_TASKS + task.getId(), task);
                operations.opsForSet().add(PREFIX_SESSIONS + task.getSessionId(), task.getId());
                return operations.exec();
            }
        });
    }

    @Override
    public Task query(String taskId) {
        return ((Task) redisTemplate.opsForValue().get(PREFIX_TASKS + taskId));
    }

    @Override
    public List<Task> queryBySessionId(String sessionId) {
        Set<Object> taskIdList = redisTemplate.opsForSet().members(PREFIX_SESSIONS + sessionId);
        if (Util.isEmpty(taskIdList)) {
            return List.of();
        }
        List<String> keys = taskIdList.stream()
                .map(taskId -> PREFIX_TASKS + taskId)
                .toList();
        List<Object> result = redisTemplate.opsForValue().multiGet(keys);
        return Optional.ofNullable(result)
                .orElse(List.of())
                .stream()
                .map(obj -> ((Task) obj))
                .toList();
    }

    @Override
    public boolean hasPushNotificationInfo(String taskId) {
        return redisTemplate.opsForValue().get(PREFIX_PUSH_NOTIFICATION_INFOS + taskId) != null;
    }

    @Override
    public PushNotificationConfig getPushNotificationInfo(String taskId) {
        Task task = this.query(taskId);
        if (task == null) {
            throw new ValueError("Task not found for " + taskId);
        }
        return ((PushNotificationConfig) redisTemplate.opsForValue().get(PREFIX_PUSH_NOTIFICATION_INFOS + taskId));
    }

    @Override
    public void setPushNotificationInfo(String taskId, PushNotificationConfig info) {
        Task task = this.query(taskId);
        if (task == null) {
            throw new ValueError("Task not found for " + taskId);
        }
        redisTemplate.opsForValue().set(PREFIX_PUSH_NOTIFICATION_INFOS + taskId, info);
    }
}
