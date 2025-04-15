package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:31
 * @desc
 */

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        // 根据此类型反序列化
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextPart.class, name = "text"),
        @JsonSubTypes.Type(value = FilePart.class, name = "file"),
        @JsonSubTypes.Type(value = DataPart.class, name = "data")
})
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Part implements Serializable {
    private String type;
    @Nullable
    private Map<String, Object> metadata;
}
