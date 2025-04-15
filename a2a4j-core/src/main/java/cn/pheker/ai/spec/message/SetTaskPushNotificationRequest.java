package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.entity.TaskPushNotificationConfig;
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
public class SetTaskPushNotificationRequest extends JsonRpcRequest<TaskPushNotificationConfig> {
    public SetTaskPushNotificationRequest() {
        this.setMethod("tasks/pushNotification/set");
    }

    public SetTaskPushNotificationRequest(TaskPushNotificationConfig params) {
        this();
        this.setParams(params);
    }
}
