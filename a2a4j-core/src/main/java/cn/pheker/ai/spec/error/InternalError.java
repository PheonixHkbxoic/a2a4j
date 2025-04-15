package cn.pheker.ai.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
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
