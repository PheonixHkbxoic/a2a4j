package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:02
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AgentSkill implements Serializable {
    private String id;
    private String name;
    @Nullable
    private String description;
    private List<String> tags;
    private List<String> examples;
    private List<String> inputModes;
    private List<String> outputModes;
}
