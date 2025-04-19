package cn.pheker.ai.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class JSONParseError extends JsonRpcError {
    public JSONParseError() {
        this.setCode(-32700);
        this.setMessage("Invalid JSON payload");
    }
}
