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
public class GetTaskPushNotificationResponse extends JsonRpcResponse<TaskPushNotificationConfig> {
    public GetTaskPushNotificationResponse() {
    }

    public GetTaskPushNotificationResponse(String id, JsonRpcError error) {
        this.setId(id);
        this.setError(error);
    }

    public GetTaskPushNotificationResponse(String id, TaskPushNotificationConfig taskPushNotificationConfig) {
        this.setId(id);
        this.setResult(taskPushNotificationConfig);
    }
}
