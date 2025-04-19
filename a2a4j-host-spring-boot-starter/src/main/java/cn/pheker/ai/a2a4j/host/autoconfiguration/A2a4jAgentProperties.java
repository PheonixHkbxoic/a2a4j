package cn.pheker.ai.a2a4j.host.autoconfiguration;

import cn.pheker.ai.a2a4j.core.spec.entity.PushNotificationConfig;
import lombok.Data;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/19 16:02
 * @desc
 */
@Data
public class A2a4jAgentProperties {
    private String baseUrl;
    private String agentCardPath;

    // maybe null
    private PushNotificationConfig notification;
}
