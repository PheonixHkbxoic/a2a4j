package cn.pheker.ai.a2a4j.core.spec.message;

import cn.pheker.ai.a2a4j.core.spec.entity.Task;
import cn.pheker.ai.a2a4j.core.spec.error.JsonRpcError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 13:26
 * @desc
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SendTaskResponse extends JsonRpcResponse<Task> {

    public SendTaskResponse() {
    }

    public SendTaskResponse(Task task) {
        this.setResult(task);
    }

    public SendTaskResponse(String id, JsonRpcError error) {
        this.setId(id);
        this.setError(error);
    }

    public SendTaskResponse(String id, JsonRpcError error, Task task) {
        this.setId(id);
        this.setError(error);
        this.setResult(task);
    }

}
