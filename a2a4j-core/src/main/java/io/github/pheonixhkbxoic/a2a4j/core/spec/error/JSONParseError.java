package io.github.pheonixhkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class JSONParseError extends JsonRpcError {
    public JSONParseError() {
        this.setCode(-32700);
        this.setMessage("Invalid JSON payload");
    }
}
