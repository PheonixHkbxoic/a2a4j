package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.Nullable;
import cn.pheker.ai.spec.error.JsonRpcError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:15
 * @desc
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Data
public class JsonRpcResponse<T> extends JsonRpcMessage {
    @Nullable
    private T result;
    @Nullable
    private JsonRpcError error;

    public JsonRpcResponse() {
    }

    public JsonRpcResponse(String id, JsonRpcError error) {
        this.setId(id);
        this.error = error;
    }
}
