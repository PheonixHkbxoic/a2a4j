package cn.pheker.ai.a2a4j.host.autoconfiguration;

import cn.pheker.ai.a2a4j.core.client.A2AClient;
import cn.pheker.ai.a2a4j.core.client.AgentCardResolver;
import cn.pheker.ai.a2a4j.core.spec.entity.AgentCard;
import cn.pheker.ai.a2a4j.core.spec.entity.PushNotificationConfig;
import cn.pheker.ai.a2a4j.core.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/18 22:28
 * @desc
 */
@Slf4j
@EnableConfigurationProperties(A2a4jAgentsProperties.class)
@Configuration(proxyBeanMethods = false)
public class A2a4jHostAutoConfiguration {

    @Bean
    public List<A2AClient> clients(A2a4jAgentsProperties a2a4jAgentsProperties) {
        PushNotificationConfig notificationGlobal = a2a4jAgentsProperties.getNotification();
        Map<String, A2a4jAgentProperties> agents = a2a4jAgentsProperties.getAgents();
        return agents.entrySet().stream()
                .map(e -> {
                    PushNotificationConfig notification = e.getValue().getNotification();

                    AgentCardResolver resolver = new AgentCardResolver(e.getKey(), e.getValue().getBaseUrl(), e.getValue().getAgentCardPath());
                    try {
                        AgentCard agentCard = resolver.resolve();
                        return new A2AClient(agentCard, notification != null ? notification : notificationGlobal);
                    } catch (Exception ex) {
                        log.error("agent resolve exception, agent: {}, error: {}", Util.toJson(resolver), ex.getMessage());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


}
