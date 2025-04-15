package cn.pheker.ai.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class ContentTypeNotSupportedError extends JsonRpcError {
    public ContentTypeNotSupportedError() {
        this.setCode(-32005);
        this.setMessage("Incompatible content types");
    }
}
