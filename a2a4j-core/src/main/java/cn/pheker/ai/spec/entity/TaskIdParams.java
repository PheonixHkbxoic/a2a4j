package cn.pheker.ai.spec.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:22
 * @desc
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskIdParams {
    private String id;
    private Map<String, Object> metadata;
}
