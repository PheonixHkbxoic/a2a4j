package io.github.PheonixHkbxoic.a2a4j.core.spec.message;

import io.github.PheonixHkbxoic.a2a4j.core.spec.Nullable;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.JsonRpcError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
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
