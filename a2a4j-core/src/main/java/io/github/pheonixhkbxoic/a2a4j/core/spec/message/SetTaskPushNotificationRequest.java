package io.github.pheonixhkbxoic.a2a4j.core.spec.message;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Method;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.TaskPushNotificationConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SetTaskPushNotificationRequest extends JsonRpcRequest<TaskPushNotificationConfig> {
    public SetTaskPushNotificationRequest() {
        this.setMethod(Method.TASKS_PUSHNOTIFICATION_SET);
    }

    public SetTaskPushNotificationRequest(TaskPushNotificationConfig params) {
        this();
        this.setParams(params);
    }
}
