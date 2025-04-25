package io.github.pheonixhkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class InternalError extends JsonRpcError {
    public InternalError() {
        this.setCode(-32603);
        this.setMessage("Internal error");
    }

    public InternalError(String message) {
        this();
        this.setMessage(message);
    }
}
