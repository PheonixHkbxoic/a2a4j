package io.github.PheonixHkbxoic.a2a4j.core.spec.message;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.TaskPushNotificationConfig;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.JsonRpcError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
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
