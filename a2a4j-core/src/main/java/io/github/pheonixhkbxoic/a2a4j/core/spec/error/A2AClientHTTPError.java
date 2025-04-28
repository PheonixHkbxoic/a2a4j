package io.github.pheonixhkbxoic.a2a4j.core.spec.error;

import lombok.Getter;

/**
 * @author PheonixHkbxoic
 */
@Getter
public class A2AClientHTTPError extends A2AClientError {
    private int statusCode;
    private String message;

    public A2AClientHTTPError(String message) {
        super(message);
    }

    public A2AClientHTTPError(int statusCode, String message) {
        super(String.format("HTTP Error %d: %s", statusCode, message));
        this.statusCode = statusCode;
        this.message = super.getMessage();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
