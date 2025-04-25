package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import lombok.*;

/**
 * @author PheonixHkbxoic
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskQueryParams extends TaskIdParams {
    private Integer historyLength;
}
