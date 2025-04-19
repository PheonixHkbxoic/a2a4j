package cn.pheker.ai.a2a4j.core.spec.message;

import cn.pheker.ai.a2a4j.core.spec.entity.TaskIdParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:17
 * @desc
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class CancelTaskRequest extends JsonRpcRequest<TaskIdParams> {
    public CancelTaskRequest() {
        this.setMethod("tasks/cancel");
    }

    public CancelTaskRequest(TaskIdParams params) {
        this();
        this.setParams(params);
    }
}
