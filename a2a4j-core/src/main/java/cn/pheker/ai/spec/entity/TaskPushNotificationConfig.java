package cn.pheker.ai.spec.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/13 14:50
 * @desc
 */
@Builder
@Data
public class TaskPushNotificationConfig implements Serializable {
    /**
     * task id
     */
    private String id;
    private PushNotificationConfig pushNotificationConfig;
}
