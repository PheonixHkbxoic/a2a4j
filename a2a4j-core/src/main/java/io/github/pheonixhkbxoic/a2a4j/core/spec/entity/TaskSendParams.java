package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TaskSendParams {
    /**
     * task id
     */
    @NotBlank
    private String id;
    // uuid 4 hex
    @NotBlank
    private String sessionId;

    @Valid
    @NotNull
    private Message message;
    @Nullable
    private List<String> acceptedOutputModes;
    @Nullable
    @Valid
    private PushNotificationConfig pushNotification;
    @Nullable
    private Integer historyLength;
    @Nullable
    private Map<String, Object> metadata;

}
