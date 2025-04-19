package cn.pheker.ai.a2a4j.core.spec.entity;

import lombok.*;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/13 14:37
 * @desc
 */
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskArtifactUpdateEvent extends UpdateEvent {
    private Artifact artifact;

    public TaskArtifactUpdateEvent(String id, Artifact artifact) {
        super.setId(id);
        this.artifact = artifact;
    }
}
