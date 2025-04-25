package io.github.pheonixhkbxoic.a2a4j.core.spec.error;

import lombok.Getter;

/**
 * @author PheonixHkbxoic
 */
@Getter
public class A2AClientJSONError extends A2AClientError {
    private String message;

    public A2AClientJSONError(String message) {
        super(String.format("JSON Error: %s", message));
        this.message = message;
    }

}
