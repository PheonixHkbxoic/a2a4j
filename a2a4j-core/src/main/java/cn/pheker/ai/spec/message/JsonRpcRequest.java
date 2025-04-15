package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:12
 * @desc
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Data
public class JsonRpcRequest<T> extends JsonRpcMessage {
    private String method;
    @Nullable
    private T params;
}
