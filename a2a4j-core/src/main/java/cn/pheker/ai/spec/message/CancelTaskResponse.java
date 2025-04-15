package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.entity.Task;
import cn.pheker.ai.spec.error.JsonRpcError;
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
