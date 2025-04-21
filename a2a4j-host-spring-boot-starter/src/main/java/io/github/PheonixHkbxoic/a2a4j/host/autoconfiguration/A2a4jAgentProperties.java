package io.github.PheonixHkbxoic.a2a4j.host.autoconfiguration;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.PushNotificationConfig;
import lombok.Data;

/**
 * @author PheonixHkbxoic
 */
@Data
public class A2a4jAgentProperties {
    private String baseUrl;
    private String agentCardPath;

    // maybe null
    private PushNotificationConfig notification;
}
