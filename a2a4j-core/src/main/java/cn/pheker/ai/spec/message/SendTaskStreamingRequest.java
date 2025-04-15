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
public class SendTaskStreamingRequest extends JsonRpcRequest<TaskSendParams> {
    public SendTaskStreamingRequest() {
        this.setMethod("tasks/sendSubscribe");
    }

    public SendTaskStreamingRequest(TaskSendParams params) {
        this();
        this.setParams(params);
    }
}
