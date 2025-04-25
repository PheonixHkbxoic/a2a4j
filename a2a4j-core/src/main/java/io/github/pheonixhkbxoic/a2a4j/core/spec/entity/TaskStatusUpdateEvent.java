package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @author PheonixHkbxoic
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskStatusUpdateEvent extends UpdateEvent {
    private TaskStatus status;
    @JsonProperty("final")
    private boolean finalFlag;


    public TaskStatusUpdateEvent(String id, TaskStatus taskStatus, boolean finalFlag) {
        super.setId(id);
        this.status = taskStatus;
        this.finalFlag = finalFlag;
    }
}
