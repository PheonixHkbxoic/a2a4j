package cn.pheker.ai.a2a4j.core.spec.message;

import cn.pheker.ai.a2a4j.core.spec.entity.TaskSendParams;
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
public class SendTaskRequest extends JsonRpcRequest<TaskSendParams> {
    public SendTaskRequest() {
        this.setMethod("tasks/send");
    }

    public SendTaskRequest(TaskSendParams params) {
        this();
        this.setParams(params);
    }
}
