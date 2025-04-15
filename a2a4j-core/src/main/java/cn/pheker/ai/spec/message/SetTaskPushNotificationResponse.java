package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.entity.TaskPushNotificationConfig;
import cn.pheker.ai.spec.error.JsonRpcError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:24
 * @desc
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Data
public class SetTaskPushNotificationResponse extends JsonRpcResponse<TaskPushNotificationConfig> {
    public SetTaskPushNotificationResponse() {
    }

    public SetTaskPushNotificationResponse(String id, JsonRpcError error) {
        this.setId(id);
        this.setError(error);
    }

    public SetTaskPushNotificationResponse(String id, TaskPushNotificationConfig taskPushNotificationConfig) {
        this.setId(id);
        this.setResult(taskPushNotificationConfig);
    }
}
