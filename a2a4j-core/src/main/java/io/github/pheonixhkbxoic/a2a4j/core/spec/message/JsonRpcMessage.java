package io.github.pheonixhkbxoic.a2a4j.core.spec.message;

import io.github.pheonixhkbxoic.a2a4j.core.util.Uuid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
@Data
public class JsonRpcMessage {
    protected final String jsonrpc = "2.0";
    /**
     * message id
     */
    @NotBlank
    protected String id;

    public JsonRpcMessage() {
        this.id = Uuid.uuid4hex();
    }
}
