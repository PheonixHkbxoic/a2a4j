package cn.pheker.ai.a2a4j.core.spec.message;

import cn.pheker.ai.a2a4j.core.spec.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:12
 * @desc
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class JsonRpcRequest<T> extends JsonRpcMessage {
    protected String method;
    @Nullable
    protected T params;
}
