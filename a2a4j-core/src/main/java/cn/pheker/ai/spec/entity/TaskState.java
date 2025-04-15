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
