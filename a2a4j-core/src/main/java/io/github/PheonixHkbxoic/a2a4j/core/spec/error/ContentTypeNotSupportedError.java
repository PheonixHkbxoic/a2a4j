package io.github.PheonixHkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class ContentTypeNotSupportedError extends JsonRpcError {
    public ContentTypeNotSupportedError() {
        this.setCode(-32005);
        this.setMessage("Incompatible content types");
    }
}
