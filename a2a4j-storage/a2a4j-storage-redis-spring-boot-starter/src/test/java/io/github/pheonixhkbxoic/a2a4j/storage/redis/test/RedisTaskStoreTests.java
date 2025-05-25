package io.github.pheonixhkbxoic.a2a4j.storage.redis.test;

import io.github.pheonixhkbxoic.a2a4j.core.core.TaskStore;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.storage.redis.RedisTaskStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author PheonixHkbxoic
 * @date 2025/5/14 19:49
 * @desc
 */
@Slf4j
public class RedisTaskStoreTests {
    static TaskStore taskStore;
    static final String sessionId = "xxx-001";

    @BeforeAll
    public static void init() {
        // Configure Redis connection
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        LettuceClientConfiguration clientConf = LettuceClientConfiguration.builder()
                .build();

        // Create connection factory
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig, clientConf);
        connectionFactory.afterPropertiesSet();


        // redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();

        taskStore = new RedisTaskStore(redisTemplate);
    }

    @Test
    public void test() {
        List<Part> parts = List.of(new TextPart("hello world"));
        Artifact artifact = Artifact.builder().parts(parts).build();
        Task task = Task.builder()
                .id("1")
                .sessionId(sessionId)
                .status(new TaskStatus(TaskState.WORKING))
                .artifacts(List.of(artifact))
                .metadata(Map.of("k", "v", "kk", 12345))
                .build();
        taskStore.insert(task);

        TaskStatus status = new TaskStatus(TaskState.COMPLETED);
        task.setStatus(status);
        taskStore.update(task);

        Task taskCompleted = taskStore.query(task.getId());
        assertThat(taskCompleted).extracting(Task::getStatus).extracting(TaskStatus::getState).isEqualTo(TaskState.COMPLETED);
        log.info("taskCompleted: {}", taskCompleted);

        Task task2 = Task.builder()
                .id("2")
                .sessionId(sessionId)
                .status(new TaskStatus(TaskState.WORKING))
                .artifacts(List.of(artifact))
                .metadata(Map.of("k2", "v2", "kk2", 12345))
                .build();
        taskStore.update(task2);

        List<Task> tasks = taskStore.queryBySessionId(sessionId);
        assertThat(tasks).size().isEqualTo(2);
        log.info("sessionId: {}, tasks: {}", sessionId, tasks);

        taskStore.delete(task.getId());
        Task taskDeleted = taskStore.query(task.getId());
        assertThat(taskDeleted).isNull();

        taskStore.deleteBySessionId(sessionId);
        List<Task> tasksDeleted = taskStore.queryBySessionId(sessionId);
        assertThat(tasksDeleted).size().isEqualTo(0);
    }

}
