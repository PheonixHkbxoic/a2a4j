package cn.pheker.ai.a2a4j.core.spec.message;

import cn.pheker.ai.a2a4j.core.spec.entity.TaskPushNotificationConfig;
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
public class SetTaskPushNotificationRequest extends JsonRpcRequest<TaskPushNotificationConfig> {
    public SetTaskPushNotificationRequest() {
        this.setMethod("tasks/pushNotification/set");
    }

    public SetTaskPushNotificationRequest(TaskPushNotificationConfig params) {
        this();
        this.setParams(params);
    }
}
