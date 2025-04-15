package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 21:56
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgentAuthentication {
    private List<String> schemes;
    @Nullable
    private String credentials;
}
