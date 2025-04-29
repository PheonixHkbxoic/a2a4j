package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 */
@ToString
@Data
public class PushNotificationConfig implements Serializable {
    @NotBlank
    private String url;
    @Nullable
    private String token;
    @Nullable
    @Valid
    private AuthenticationInfo authentication;
}
