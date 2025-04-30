package io.github.pheonixhkbxoic.a2a4j.core.spec.message;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Method;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.TaskSendParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SendTaskStreamingRequest extends JsonRpcRequest<TaskSendParams> {
    public SendTaskStreamingRequest() {
        this.setMethod(Method.TASKS_SENDSUBSCRIBE);
    }

    public SendTaskStreamingRequest(TaskSendParams params) {
        this();
        this.setParams(params);
    }
}
