package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @Valid
    @NotNull
    private Role role;
    @Valid
    @NotNull
    @NotEmpty
    private List<Part> parts;
    @Nullable
    private Map<String, Object> metadata;

}
