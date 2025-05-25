package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import jakarta.validation.constraints.NotBlank;
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
public class TextPart extends Part implements Serializable {
    @NotBlank
    private String text;

    public TextPart() {
        this.type = Part.TEXT;
    }

    public TextPart(String text) {
        this.type = Part.TEXT;
        this.text = text;
    }

    public TextPart(String text, Map<String, Object> metadata) {
        this.type = Part.TEXT;
        this.text = text;
        this.metadata = metadata;
    }
}
