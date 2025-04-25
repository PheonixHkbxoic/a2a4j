package io.github.pheonixhkbxoic.a2a4j.agent.webflux.autoconfiguration;


import io.github.pheonixhkbxoic.a2a4j.core.core.PushNotificationSenderAuth;
import io.github.pheonixhkbxoic.a2a4j.core.core.TaskManager;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.pheonixhkbxoic.a2a4j.webflux.WebfluxSseServerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author PheonixHkbxoic
 */
@ConditionalOnClass(value = {AgentCard.class, TaskManager.class})
@Configuration
public class A2a4jAgentWebfluxAutoConfiguration {

    @Bean
    public PushNotificationSenderAuth pushNotificationSenderAuth() {
        return new PushNotificationSenderAuth();
    }

    @Bean
    public WebfluxSseServerAdapter webMvcSseServerAdapter(AgentCard agentCard, TaskManager taskManager, PushNotificationSenderAuth pushNotificationSenderAuth) {
        return new WebfluxSseServerAdapter(agentCard, taskManager, null, pushNotificationSenderAuth);
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction(WebfluxSseServerAdapter serverAdapter) {
        return serverAdapter.getRouterFunction();
    }


}
