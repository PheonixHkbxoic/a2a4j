package cn.pheker.ai.spec.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/13 14:37
 * @desc
 */

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskStatusUpdateEvent extends UpdateEvent {
    private TaskStatus status;
    @JsonProperty("final")
    private boolean finalFlag;


    public TaskStatusUpdateEvent(String id, TaskStatus taskStatus, boolean finalFlag) {
        this.setId(id);
        this.status = taskStatus;
        this.finalFlag = finalFlag;
    }
}
