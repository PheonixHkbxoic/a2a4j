package io.github.pheonixhkbxoic.a2a4j.core.test;

import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@Slf4j
public class TestJackson {

    @Test
    public void testSerialize() {
        TextPart textPart = new TextPart("abc");
        System.out.println("textPart = " + textPart);
        String json = Util.toJson(textPart);
        System.out.println("textPart = " + json);
        Part part = Util.fromJson(json, Part.class);
        System.out.println("part = " + part);
    }

    @Test
    public void testTaskStatus() {
        TaskStatus taskStatus = new TaskStatus(TaskState.WORKING);
        System.out.println("taskStatus = " + taskStatus);
        String json = Util.toJson(taskStatus);
        System.out.println("taskStatus = " + json);
        TaskStatus copy = Util.fromJson(json, TaskStatus.class);
        System.out.println("copy = " + copy);
    }

    @Test
    public void testPart() {
        FileContent content = new FileContent();
        content.setName("xyz.pdf");
        Message message = Message.builder()
                .role(Role.USER)
                .parts(List.of(new TextPart("abc"), new DataPart(Map.of("kk", 1)), new FilePart(content)))
                .build();
        TaskSendParams taskSendParams = TaskSendParams.builder().message(message).build();
        String json = Util.toJson(taskSendParams);
        log.info("json: {}", json);
        TaskSendParams deepCopy = Util.fromJson(json, TaskSendParams.class);
        log.info("deepCopy: {}", deepCopy);
    }
}
