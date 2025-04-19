package cn.pheker.ai.a2a4j.core.spec.error;

import cn.pheker.ai.a2a4j.core.spec.Nullable;
import lombok.Data;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:14
 * @desc
 */
@Data
public class JsonRpcError {
    private int code;
    private String message;
    @Nullable
    private Object data;
}
