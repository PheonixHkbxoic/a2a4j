package io.github.pheonixhkbxoic.a2a4j.core.spec.message;

import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Task;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.JsonRpcError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class CancelTaskResponse extends JsonRpcResponse<Task> {
    public CancelTaskResponse(String id, JsonRpcError error) {
        this.setId(id);
        this.setError(error);
    }

    public CancelTaskResponse(String id, Task taskResult) {
        this.setId(id);
        this.setResult(taskResult);
    }
}
