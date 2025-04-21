package io.github.PheonixHkbxoic.a2a4j.core.spec.error;

import io.github.PheonixHkbxoic.a2a4j.core.spec.Nullable;
import lombok.Data;

/**
 * @author PheonixHkbxoic
 */
@Data
public class JsonRpcError {
    private int code;
    private String message;
    @Nullable
    private Object data;
}
