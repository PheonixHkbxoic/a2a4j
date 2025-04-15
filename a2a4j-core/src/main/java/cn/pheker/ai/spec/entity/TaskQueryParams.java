package cn.pheker.ai.spec.entity;

import lombok.*;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:23
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskQueryParams extends TaskIdParams {
    private Integer historyLength;
}
