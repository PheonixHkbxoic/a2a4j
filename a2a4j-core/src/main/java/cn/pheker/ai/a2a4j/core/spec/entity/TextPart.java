package cn.pheker.ai.a2a4j.core.spec.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:32
 * @desc
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class TextPart extends Part implements Serializable {
    private String text;

    public TextPart() {
        this.type = "text";
    }

    public TextPart(String text) {
        this.type = "text";
        this.text = text;
    }

    public TextPart(String type, Map<String, Object> metadata, String text) {
        super(type, metadata);
        this.text = text;
    }
}
