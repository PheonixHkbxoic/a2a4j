package cn.pheker.ai.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class InvalidRequestError extends JsonRpcError {
    public InvalidRequestError() {
        this.setCode(-32600);
        this.setMessage("Request payload validation error");
    }
}
