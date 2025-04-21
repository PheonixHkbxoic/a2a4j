package io.github.PheonixHkbxoic.a2a4j.core.spec.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 */
@ToString
@Getter
public enum TaskState implements Serializable {
    SUBMITTED("submitted"),
    WORKING("working"),
    INPUT_REQUIRED("input-required"),
    COMPLETED("completed"),
    CANCELED("canceled"),
    FAILED("failed"),
    UNKNOWN("unknown");

    private final String state;

    TaskState(String state) {
        this.state = state;
    }

    @JsonValue
    public String getState() {
        return state;
    }
}
