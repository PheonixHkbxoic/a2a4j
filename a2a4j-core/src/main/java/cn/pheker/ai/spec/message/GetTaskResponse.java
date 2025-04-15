package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.entity.Task;
import cn.pheker.ai.spec.error.TaskNotFoundError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:24
 * @desc
 */
@ToString
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
