package cn.pheker.ai.spec.entity;

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
public class TaskArtifactUpdateEvent extends UpdateEvent {
    private Artifact artifact;

    public TaskArtifactUpdateEvent(String id, Artifact artifact) {
        this.setId(id);
        this.artifact = artifact;
    }
}
