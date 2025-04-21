package io.github.PheonixHkbxoic.a2a4j.core.spec.message;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.TaskIdParams;
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
        this.setMethod("tasks/pushNotification/get");
    }

    public GetTaskPushNotificationRequest(TaskIdParams params) {
        this();
        this.setParams(params);
    }
}
