package io.github.PheonixHkbxoic.a2a4j.agent.mvc.autoconfiguration;


import io.github.PheonixHkbxoic.a2a4j.core.core.PushNotificationSenderAuth;
import io.github.PheonixHkbxoic.a2a4j.core.core.TaskManager;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.PheonixHkbxoic.a2a4j.mvc.WebMvcSseServerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author PheonixHkbxoic
 */
@ConditionalOnClass(value = {AgentCard.class, TaskManager.class})
@Configuration
public class A2a4jAgentMvcAutoConfiguration {

    @Bean
    public PushNotificationSenderAuth pushNotificationSenderAuth() {
        return new PushNotificationSenderAuth();
    }

    @Bean
    public WebMvcSseServerAdapter webMvcSseServerAdapter(AgentCard agentCard, TaskManager taskManager, PushNotificationSenderAuth pushNotificationSenderAuth) {
        return new WebMvcSseServerAdapter(agentCard, taskManager, null, pushNotificationSenderAuth);
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction(WebMvcSseServerAdapter serverAdapter) {
        return serverAdapter.getRouterFunction();
    }


}
