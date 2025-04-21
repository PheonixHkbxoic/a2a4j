package io.github.PheonixHkbxoic.a2a4j.core.spec.message;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.TaskSendParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
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
