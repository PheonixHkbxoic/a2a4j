package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:44
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Message implements Serializable {
    private Role role;
    private List<Part> parts;
    @Nullable
    private Map<String, Object> metadata;

}
