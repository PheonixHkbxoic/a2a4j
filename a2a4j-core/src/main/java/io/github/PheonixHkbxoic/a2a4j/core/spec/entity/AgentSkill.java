package io.github.PheonixHkbxoic.a2a4j.core.spec.entity;

import io.github.PheonixHkbxoic.a2a4j.core.spec.Nullable;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author PheonixHkbxoic
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AgentSkill implements Serializable {
    private String id;
    private String name;
    @Nullable
    private String description;
    private List<String> tags;
    private List<String> examples;
    private List<String> inputModes;
    private List<String> outputModes;
}
