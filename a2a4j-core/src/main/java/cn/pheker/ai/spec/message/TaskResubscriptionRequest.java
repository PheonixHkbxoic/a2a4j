package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.entity.TaskIdParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:17
 * @desc
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskResubscriptionRequest extends JsonRpcRequest<TaskIdParams> {
    public TaskResubscriptionRequest() {
        this.setMethod("tasks/resubscribe");
    }

    public TaskResubscriptionRequest(TaskIdParams params) {
        this();
        this.setParams(params);
    }
}
