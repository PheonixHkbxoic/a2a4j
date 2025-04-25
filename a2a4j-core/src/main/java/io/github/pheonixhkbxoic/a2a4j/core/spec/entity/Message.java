package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
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
@Builder
@Data
public class Message implements Serializable {
    private Role role;
    private List<Part> parts;
    @Nullable
    private Map<String, Object> metadata;

}
