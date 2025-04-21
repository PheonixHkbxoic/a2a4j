package io.github.PheonixHkbxoic.a2a4j.host.autoconfiguration;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.PushNotificationConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@Data
@ConfigurationProperties(prefix = "a2a4j.host")
public class A2a4jAgentsProperties {
    /**
     * host global push notification config
     */
    private PushNotificationConfig notification;

    /**
     * remote agents config
     */
    private Map<String, A2a4jAgentProperties> agents;
}
