package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author PheonixHkbxoic
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgentAuthentication {
    private List<String> schemes;
    @Nullable
    private String credentials;
}
