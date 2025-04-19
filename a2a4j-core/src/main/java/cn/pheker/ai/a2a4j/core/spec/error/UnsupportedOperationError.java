package cn.pheker.ai.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class UnsupportedOperationError extends JsonRpcError {
    public UnsupportedOperationError() {
        this.setCode(-32004);
        this.setMessage("This operation is not supported");
    }
}
