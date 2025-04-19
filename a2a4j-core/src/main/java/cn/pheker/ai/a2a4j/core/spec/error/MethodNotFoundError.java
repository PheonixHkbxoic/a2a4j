package cn.pheker.ai.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class MethodNotFoundError extends JsonRpcError {
    public MethodNotFoundError() {
        this.setCode(-32601);
        this.setMessage("Method not found");
    }
}
