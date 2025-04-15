package cn.pheker.ai.spec.error;

import lombok.Getter;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/13 00:05
 * @desc
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
        this.message = message;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
