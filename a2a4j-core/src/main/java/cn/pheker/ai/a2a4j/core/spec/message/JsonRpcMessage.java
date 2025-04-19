package cn.pheker.ai.a2a4j.core.spec.message;

import cn.pheker.ai.a2a4j.core.util.Uuid;
import lombok.Data;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:07
 * @desc
 */
@ToString(callSuper = true)
@Data
public class JsonRpcMessage {
    protected final String jsonrpc = "2.0";
    /**
     * message id
     */
    protected String id;

    public JsonRpcMessage() {
        this.id = Uuid.uuid4hex();
    }
}
