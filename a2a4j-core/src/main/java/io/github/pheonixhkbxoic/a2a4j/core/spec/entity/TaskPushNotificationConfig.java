package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 */
@ToString
@Builder
@Data
public class TaskPushNotificationConfig implements Serializable {
    /**
     * task id
     */
    private String id;
    private PushNotificationConfig pushNotificationConfig;
}
