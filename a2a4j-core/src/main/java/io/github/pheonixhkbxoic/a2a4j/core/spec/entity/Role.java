package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 */
@ToString
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
