package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.entity.TaskSendParams;
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
public class SendTaskRequest extends JsonRpcRequest<TaskSendParams> {
    public SendTaskRequest() {
        this.setMethod("tasks/send");
    }

    public SendTaskRequest(TaskSendParams params) {
        this();
        this.setParams(params);
    }
}
