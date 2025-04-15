package cn.pheker.ai.spec.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:40
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataPart extends Part implements Serializable {
    private Map<String, Object> data;

    public DataPart() {
        this.setType("data");
    }

    public DataPart(String type, Map<String, Object> metadata, Map<String, Object> data) {
        super(type, metadata);
        this.data = data;
    }
}
