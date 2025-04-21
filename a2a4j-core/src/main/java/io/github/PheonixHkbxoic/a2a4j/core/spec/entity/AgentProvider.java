package io.github.PheonixHkbxoic.a2a4j.core.spec.entity;

import io.github.PheonixHkbxoic.a2a4j.core.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class AgentProvider implements Serializable {
    private String organization;
    @Nullable
    private String url;
}
