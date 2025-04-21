package io.github.PheonixHkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class UnsupportedOperationError extends JsonRpcError {
    public UnsupportedOperationError() {
        this.setCode(-32004);
        this.setMessage("This operation is not supported");
    }
}
