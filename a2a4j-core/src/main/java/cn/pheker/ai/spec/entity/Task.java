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
 * @date 2025/4/10 22:20
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Task implements Serializable {
    private String id;
    @Nullable
    private String sessionId;
    private TaskStatus status;
    @Nullable
    private List<Artifact> artifacts;
    @Nullable
    private List<Message> history;
    @Nullable
    private Map<String, Object> metadata;

}
