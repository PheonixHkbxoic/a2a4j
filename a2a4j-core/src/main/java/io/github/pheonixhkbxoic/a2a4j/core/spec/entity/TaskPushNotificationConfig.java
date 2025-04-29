package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    private String id;
    @Valid
    @NotNull
    private PushNotificationConfig pushNotificationConfig;
}
