package cn.pheker.ai.a2a4j.core.spec.entity;

import cn.pheker.ai.a2a4j.core.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/19 17:51
 * @desc
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
