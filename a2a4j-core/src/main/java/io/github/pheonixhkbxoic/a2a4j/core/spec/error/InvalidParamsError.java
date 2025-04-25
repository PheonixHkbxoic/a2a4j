package io.github.pheonixhkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class InvalidParamsError extends JsonRpcError {
    public InvalidParamsError() {
        this.setCode(-32602);
        this.setMessage("Invalid parameters");
    }

    public InvalidParamsError(String message) {
        this();
        this.setMessage(message);
    }
}
