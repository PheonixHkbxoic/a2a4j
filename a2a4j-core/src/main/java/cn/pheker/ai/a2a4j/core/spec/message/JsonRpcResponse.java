package cn.pheker.ai.a2a4j.core.spec.message;

import cn.pheker.ai.a2a4j.core.spec.Nullable;
import cn.pheker.ai.a2a4j.core.spec.error.JsonRpcError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:15
 * @desc
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class JsonRpcResponse<T> extends JsonRpcMessage {
    @Nullable
    protected T result;
    @Nullable
    protected JsonRpcError error;

    public JsonRpcResponse() {
    }

    public JsonRpcResponse(String id, JsonRpcError error) {
        this.setId(id);
        this.error = error;
    }
}
