package cn.pheker.ai.spec.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:26
 * @desc
 */
@Getter
public enum Role implements Serializable {
    USER("user"),
    AGENT("agent"),
    ;
    private final String desc;

    Role(String desc) {
        this.desc = desc;
    }

    @JsonValue
    public String getDesc() {
        return desc;
    }
}
