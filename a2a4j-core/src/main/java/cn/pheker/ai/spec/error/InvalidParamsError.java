package cn.pheker.ai.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class InvalidParamsError extends JsonRpcError {
    public InvalidParamsError() {
        this.setCode(-32602);
        this.setMessage("Invalid parameters");
    }

    public InvalidParamsError(String message) {
        this();
        this.setMessage(message);
    }
}
