package io.github.pheonixhkbxoic.a2a4j.agent.webflux.autoconfiguration;


import io.github.pheonixhkbxoic.a2a4j.core.core.*;
import io.github.pheonixhkbxoic.a2a4j.core.server.A2AServer;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.pheonixhkbxoic.a2a4j.webflux.WebfluxSseServerAdapter;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    @ConditionalOnMissingBean(PushNotificationSenderAuth.class)
    @ConditionalOnClass(PushNotificationSenderAuth.class)
    @Bean
    public PushNotificationSenderAuth pushNotificationSenderAuth() {
        return new PushNotificationSenderAuth();
    }

    @ConditionalOnMissingBean(TaskStore.class)
    @ConditionalOnClass(InMemoryTaskStore.class)
    @Bean
    public InMemoryTaskStore inMemoryTaskStore() {
        return new InMemoryTaskStore();
    }

    @ConditionalOnMissingBean(ServerAdapter.class)
    @ConditionalOnClass(ServerAdapter.class)
    @Bean
    public WebfluxSseServerAdapter webfluxSseServerAdapter(AgentCard agentCard, TaskManager taskManager, Validator validator, PushNotificationSenderAuth pushNotificationSenderAuth) {
        return new WebfluxSseServerAdapter(agentCard, taskManager, validator, pushNotificationSenderAuth);
    }

    @ConditionalOnMissingBean(A2AServer.class)
    @ConditionalOnClass(A2AServer.class)
    @Bean
    public A2AServer a2aServer(WebfluxSseServerAdapter webfluxSseServerAdapter) {
        return webfluxSseServerAdapter.getServer().start();
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction(WebfluxSseServerAdapter serverAdapter) {
        return serverAdapter.getRouterFunction();
    }


}
