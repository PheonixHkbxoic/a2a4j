package cn.pheker.ai.spec.message;

import cn.pheker.ai.util.Uuid;
import lombok.Data;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:07
 * @desc
 */
@ToString
@Data
public class JsonRpcMessage {
    private final String jsonrpc = "2.0";
    /**
     * message id
     */
    private String id;

    public JsonRpcMessage() {
        this.id = Uuid.uuid4hex();
    }
}
