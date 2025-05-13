package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class DataPart extends Part implements Serializable {
    @NotEmpty
    private Map<String, Object> data;

    public DataPart() {
        this.type = Part.DATA;
    }

    public DataPart(Map<String, Object> data) {
        this.type = Part.DATA;
        this.data = data;
    }

    public DataPart(Map<String, Object> data, Map<String, Object> metadata) {
        this.type = Part.DATA;
        this.data = data;
        this.metadata = metadata;
    }
}
