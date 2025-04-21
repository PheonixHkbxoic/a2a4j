package io.github.PheonixHkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class InvalidRequestError extends JsonRpcError {
    public InvalidRequestError() {
        this.setCode(-32600);
        this.setMessage("Request payload validation error");
    }
}
