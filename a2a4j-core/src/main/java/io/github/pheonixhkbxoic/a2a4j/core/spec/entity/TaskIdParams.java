package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskIdParams {
    @NotBlank
    private String id;
    private Map<String, Object> metadata;
}
