package io.github.pheonixhkbxoic.a2a4j.core.spec.message;

import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.TaskIdParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
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
