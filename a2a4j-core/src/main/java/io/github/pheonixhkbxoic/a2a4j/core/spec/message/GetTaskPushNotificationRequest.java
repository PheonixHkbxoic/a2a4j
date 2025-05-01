package io.github.pheonixhkbxoic.a2a4j.core.spec.message;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Method;
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
public class GetTaskPushNotificationRequest extends JsonRpcRequest<TaskIdParams> {
    public GetTaskPushNotificationRequest() {
        this.setMethod(Method.TASKS_PUSHNOTIFICATION_GET);
    }

    public GetTaskPushNotificationRequest(TaskIdParams params) {
        this();
        this.setParams(params);
    }
}
