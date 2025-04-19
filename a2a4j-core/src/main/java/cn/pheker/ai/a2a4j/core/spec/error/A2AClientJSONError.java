package cn.pheker.ai.a2a4j.core.spec.error;

import lombok.Getter;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/13 00:05
 * @desc
 */
@Getter
public class A2AClientJSONError extends A2AClientError {
    private String message;

    public A2AClientJSONError(String message) {
        super(String.format("JSON Error: %s", message));
        this.message = message;
    }

}
