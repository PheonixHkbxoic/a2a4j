package io.github.PheonixHkbxoic.a2a4j.core.spec.entity;

import io.github.PheonixHkbxoic.a2a4j.core.spec.Nullable;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Task implements Serializable {
    private String id;
    @Nullable
    private String sessionId;
    private TaskStatus status;
    @Nullable
    private List<Artifact> artifacts;
    @Nullable
    private List<Message> history;
    @Nullable
    private Map<String, Object> metadata;

}
