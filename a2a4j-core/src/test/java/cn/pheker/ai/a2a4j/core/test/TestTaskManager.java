package cn.pheker.ai.a2a4j.core.test;

import cn.pheker.ai.a2a4j.core.spec.entity.*;
import cn.pheker.ai.a2a4j.core.spec.message.SendTaskRequest;
import cn.pheker.ai.a2a4j.core.spec.message.SendTaskResponse;
import cn.pheker.ai.a2a4j.core.util.Uuid;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.*;
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
}
