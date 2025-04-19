package cn.pheker.ai.a2a4j.core.spec.message;

import cn.pheker.ai.a2a4j.core.spec.entity.Task;
import cn.pheker.ai.a2a4j.core.spec.error.TaskNotFoundError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:24
 * @desc
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
