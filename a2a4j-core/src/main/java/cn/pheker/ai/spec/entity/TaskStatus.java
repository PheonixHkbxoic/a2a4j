package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:48
 * @desc
 */
@Data
public class TaskStatus implements Serializable {
    private TaskState state;
    @Nullable
    private Message message;

    // 2025-04-10T23:02:20.390873
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;

    public TaskStatus() {
    }

    public TaskStatus(TaskState state) {
        this.state = state;
        this.timestamp = LocalDateTime.now();
    }

    public TaskStatus(TaskState state, Message message) {
        this.state = state;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
