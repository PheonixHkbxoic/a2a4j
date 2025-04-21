package io.github.PheonixHkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class MethodNotFoundError extends JsonRpcError {
    public MethodNotFoundError() {
        this.setCode(-32601);
        this.setMessage("Method not found");
    }
}
