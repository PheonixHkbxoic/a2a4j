package io.github.PheonixHkbxoic.a2a4j.core.test;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.Part;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.TaskState;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.TaskStatus;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.TextPart;
import io.github.PheonixHkbxoic.a2a4j.core.util.Util;
import org.junit.jupiter.api.Test;

/**
 * @author PheonixHkbxoic
 */
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

}
