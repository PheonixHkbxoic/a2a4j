package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 */

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        // 根据此类型反序列化
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextPart.class, name = "text"),
        @JsonSubTypes.Type(value = FilePart.class, name = "file"),
        @JsonSubTypes.Type(value = DataPart.class, name = "data")
})
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Part implements Serializable {
    public static String TEXT = "text";
    public static String FILE = "file";
    public static String DATA = "data";

    @NotBlank
    protected String type;
    @Nullable
    protected Map<String, Object> metadata;
}
