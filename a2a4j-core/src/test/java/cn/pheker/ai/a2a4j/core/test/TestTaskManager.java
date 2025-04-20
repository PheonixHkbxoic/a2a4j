package cn.pheker.ai.a2a4j.core.test;

import cn.pheker.ai.a2a4j.core.core.InMemoryTaskManager;
import cn.pheker.ai.a2a4j.core.spec.entity.*;
import cn.pheker.ai.a2a4j.core.spec.message.*;
import cn.pheker.ai.a2a4j.core.util.Uuid;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 13:32
 * @desc
 */
@Slf4j
public class TestTaskManager {


    @Test
    public void testEchoTaskManager() {
        EchoAgent agent = new EchoAgent();
        EchoTaskManager echo = new EchoTaskManager(agent);

        SendTaskRequest request = new SendTaskRequest();
        Map<String, Object> metadata = new HashMap<String, Object>() {{
            put("from", "user");
            put("target", "test");
            put("other", 1);
        }};
        List<Part> parts = Arrays.asList(new TextPart("what is a2a?"), new TextPart("show me sample with java"));
        Message message = Message.builder().role(Role.USER).metadata(metadata).parts(parts).build();
        TaskSendParams params = TaskSendParams.builder()
                .id(Uuid.uuid4hex())
                .sessionId(Uuid.uuid4hex())
                .acceptedOutputModes(Collections.singletonList("text"))
                .historyLength(5)
                .message(message)
                .build();
        request.setParams(params);
        SendTaskResponse response = echo.onSendTask(request);
        log.info("response: {}", response.getResult().getArtifacts().stream()
                .flatMap(a -> a.getParts().stream())
                .filter(p -> p instanceof TextPart)
                .map(p -> ((TextPart) p).getText())
                .collect(Collectors.joining()));
    }

    @Test
    public void testQueue() {
        InMemoryTaskManager manager = new InMemoryTaskManager() {
            @Override
            public SendTaskResponse onSendTask(SendTaskRequest request) {
                return null;
            }

            @Override
            public Mono<JsonRpcResponse> onSendTaskSubscribe(SendTaskStreamingRequest request) {
                final String taskId = Uuid.uuid4hex();
                this.initEventQueue(taskId, false);

                log.info("enqueue flux push before: {}", taskId);
                Flux.<Integer>push(sink -> {
                            for (int i = 0; i < 10; i++) {
                                sink.next(i + 1);
//                                try {
//                                    TimeUnit.MILLISECONDS.sleep(500);
//                                } catch (InterruptedException e) {
//                                    throw new RuntimeException(e);
//                                }
                            }
                            sink.complete();
                        })
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(n -> {
                            log.info("enqueue n: {}", n);
                            TaskStatusUpdateEvent e = new TaskStatusUpdateEvent(new TaskStatus(TaskState.WORKING), n == 10);
                            e.setMetadata(Collections.singletonMap("n", n));
                            this.enqueueEvent(taskId, e);
                        });
                log.info("enqueue flux subscribe after: {}", taskId);

                this.dequeueEvent(taskId)
                        .subscribe(e -> {
                            log.info("dequeue subscribe, n: {}", e.getMetadata().get("n"));
                        });
                log.info("dequeue flux subscribe after: {}", taskId);

                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            public Mono<JsonRpcResponse> onResubscribeTask(TaskResubscriptionRequest request) {
                return null;
            }
        };

        manager.onSendTaskSubscribe(null);
    }


}
