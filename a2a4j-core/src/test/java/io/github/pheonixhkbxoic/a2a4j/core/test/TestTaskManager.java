package io.github.pheonixhkbxoic.a2a4j.core.test;

import io.github.pheonixhkbxoic.a2a4j.core.core.*;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskRequest;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskResponse;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskStreamingRequest;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import io.github.pheonixhkbxoic.a2a4j.core.util.Uuid;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author PheonixHkbxoic
 */
@Slf4j
public class TestTaskManager {


    @Test
    public void testEchoTaskManager() {
        TaskManager taskManager = getTaskManager();

        SendTaskRequest request = new SendTaskRequest();
        Map<String, Object> metadata = new HashMap<>() {{
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

        // onSendTask
        request.setParams(params);
        SendTaskResponse response = taskManager.onSendTask(request).block();
        assert response != null;
        log.info("response: {}", response.getResult().getArtifacts().stream()
                .flatMap(a -> a.getParts().stream())
                .filter(p -> p instanceof TextPart)
                .map(p -> ((TextPart) p).getText())
                .collect(Collectors.joining()));

        SendTaskStreamingRequest requestStream = new SendTaskStreamingRequest(params);
        taskManager.onSendTaskSubscribe(requestStream)
                .switchIfEmpty(Mono.fromRunnable(() -> taskManager.dequeueEvent(params.getId())
                        .subscribe(updateEvent -> {
                            if (updateEvent instanceof TaskStatusUpdateEvent statusUpdateEvent) {
                                TaskStatus status = statusUpdateEvent.getStatus();
                                TaskState state = status.getState();
                                LocalDateTime timestamp = status.getTimestamp();
                                Message m = status.getMessage();
                                String text = Stream.ofNullable(m)
                                        .filter(Objects::nonNull)
                                        .flatMap(t -> t.getParts().stream())
                                        .filter(p -> p instanceof TextPart)
                                        .map(t -> ((TextPart) t).getText())
                                        .filter(t -> !Util.isEmpty(t))
                                        .collect(Collectors.joining("\n"));
                                log.info("status event: {}, {}, {}", state, timestamp.format(DateTimeFormatter.ISO_DATE_TIME), text);
                            } else if (updateEvent instanceof TaskArtifactUpdateEvent artifactUpdateEvent) {
                                Artifact artifact = artifactUpdateEvent.getArtifact();
                                String text = Stream.ofNullable(artifact)
                                        .filter(Objects::nonNull)
                                        .flatMap(t -> t.getParts().stream())
                                        .filter(p -> p instanceof TextPart)
                                        .map(t -> ((TextPart) t).getText())
                                        .collect(Collectors.joining("\n"));
                                log.info("artifact event: {}", text);
                            }
                        })))
                .block();
    }

    private static TaskManager getTaskManager() {
        InMemoryTaskStore taskStore = new InMemoryTaskStore();
        PushNotificationSenderAuth pushNotificationSenderAuth = new PushNotificationSenderAuth();
        EchoAgent agent = new EchoAgent();
        return new DefaultTaskManager(taskStore, pushNotificationSenderAuth, new AgentInvoker() {
            @Override
            public Mono<List<Artifact>> invoke(SendTaskRequest request) {
                String userQuery = this.extractUserQuery(request.getParams());
                return agent.chat(userQuery)
                        .map(text -> {
                            Artifact artifact = Artifact.builder().name("answer").parts(List.of(new TextPart(text))).build();
                            return List.of(artifact);
                        });
            }

            @Override
            public Flux<StreamData> invokeStream(SendTaskStreamingRequest request) {
                String userQuery = this.extractUserQuery(request.getParams());
                return agent.chatStream(userQuery)
                        .map(text -> {
                            Message message = Message.builder().role(Role.AGENT).parts(List.of(new TextPart(text))).build();
                            return StreamData.builder().state(TaskState.WORKING).message(message).endStream(false).build();
                        })
                        .concatWithValues(StreamData.builder()
                                .state(TaskState.COMPLETED)
                                .message(Message.builder().role(Role.AGENT).parts(List.of(new TextPart(""))).build())
                                .endStream(true)
                                .build());
            }
        });
    }

}
