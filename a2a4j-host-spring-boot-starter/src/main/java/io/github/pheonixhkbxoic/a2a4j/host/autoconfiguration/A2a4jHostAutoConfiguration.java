package io.github.pheonixhkbxoic.a2a4j.host.autoconfiguration;

import io.github.pheonixhkbxoic.a2a4j.core.client.A2AClient;
import io.github.pheonixhkbxoic.a2a4j.core.client.A2AClientSet;
import io.github.pheonixhkbxoic.a2a4j.core.client.AgentCardResolver;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.PushNotificationConfig;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author PheonixHkbxoic
 */
@Slf4j
@EnableConfigurationProperties(A2a4jAgentsProperties.class)
@Configuration(proxyBeanMethods = false)
public class A2a4jHostAutoConfiguration {

    @ConditionalOnMissingBean(A2AClientSet.class)
    @ConditionalOnClass(A2AClientSet.class)
    @Bean
    public A2AClientSet a2aClientSet(A2a4jAgentsProperties a2a4jAgentsProperties) {
        PushNotificationConfig notificationGlobal = a2a4jAgentsProperties.getNotification();
        Map<String, A2a4jAgentProperties> agents = a2a4jAgentsProperties.getAgents();
        Map<String, A2AClient> clientMap = agents.entrySet().stream()
                .map(e -> {
                    PushNotificationConfig notification = e.getValue().getNotification();

                    AgentCardResolver resolver = new AgentCardResolver(e.getKey(), e.getValue().getBaseUrl(), e.getValue().getAgentCardPath());
                    try {
                        AgentCard agentCard = resolver.resolve();
                        return Map.entry(e.getKey(), new A2AClient(agentCard, notification != null ? notification : notificationGlobal));
                    } catch (Exception ex) {
                        log.error("agent resolve exception, agent: {}, error: {}", Util.toJson(resolver), ex.getMessage());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        A2AClientSet a2aClientSet = new A2AClientSet(clientMap);
        log.info("A2AClientSet initialized, names: {}", a2aClientSet.toNameMap().keySet());
        return a2aClientSet;
    }


}
