package cn.pheker.ai.a2a4j.core.test;

import cn.pheker.ai.a2a4j.core.spec.entity.Part;
import cn.pheker.ai.a2a4j.core.spec.entity.TaskState;
import cn.pheker.ai.a2a4j.core.spec.entity.TaskStatus;
import cn.pheker.ai.a2a4j.core.spec.entity.TextPart;
import cn.pheker.ai.a2a4j.core.util.Util;
import org.junit.jupiter.api.Test;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/19 20:10
 * @desc
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
