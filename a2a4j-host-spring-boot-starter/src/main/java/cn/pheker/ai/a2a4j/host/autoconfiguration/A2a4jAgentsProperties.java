package cn.pheker.ai.a2a4j.host.autoconfiguration;

import cn.pheker.ai.a2a4j.core.spec.entity.PushNotificationConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/18 22:37
 * @desc
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
