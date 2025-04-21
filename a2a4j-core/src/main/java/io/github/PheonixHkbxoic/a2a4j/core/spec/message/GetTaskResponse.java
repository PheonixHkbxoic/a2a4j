package io.github.PheonixHkbxoic.a2a4j.core.spec.message;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.Task;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.TaskNotFoundError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class GetTaskResponse extends JsonRpcResponse<Task> {
    public GetTaskResponse() {
    }

    public GetTaskResponse(String id, TaskNotFoundError taskNotFoundError) {
        this.setId(id);
        this.setError(taskNotFoundError);
    }

    public GetTaskResponse(String id, Task taskResult) {
        this.setId(id);
        this.setResult(taskResult);
    }
}
