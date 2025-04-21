package io.github.PheonixHkbxoic.a2a4j.core.spec.entity;

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
    private String id;
    private Map<String, Object> metadata;
}
