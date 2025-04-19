package cn.pheker.ai.a2a4j.core.spec.entity;

import cn.pheker.ai.a2a4j.core.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 21:56
 * @desc
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
