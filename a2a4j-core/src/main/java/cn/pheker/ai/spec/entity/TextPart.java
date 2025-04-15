package cn.pheker.ai.spec.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:32
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TextPart extends Part implements Serializable {
    private String text;

    public TextPart() {
        this.setType("text");
    }

    public TextPart(String text) {
        this();
        this.text = text;
    }

    public TextPart(String type, Map<String, Object> metadata, String text) {
        super(type, metadata);
        this.text = text;
    }
}
