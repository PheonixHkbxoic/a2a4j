package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TaskStatusUpdateEvent.class, name = "status"),
        @JsonSubTypes.Type(value = TaskArtifactUpdateEvent.class, name = "artifact")
})
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateEvent {
    protected String id;
    protected Map<String, Object> metadata;
}
