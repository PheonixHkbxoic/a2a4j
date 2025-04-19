package cn.pheker.ai.a2a4j.core.spec.entity;

import cn.pheker.ai.a2a4j.core.spec.Nullable;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:22
 * @desc
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Artifact implements Serializable {
    @Nullable
    private String name;
    @Nullable
    private String description;
    private List<Part> parts;
    @Nullable
    private Map<String, Object> metadata;
    private int index;
    @Nullable
    private Boolean append;
    @Nullable
    private Boolean lastChunk;

    public Artifact(List<Part> parts, int index, Boolean append) {
        this.parts = parts;
        this.index = index;
        this.append = append;
    }
}
