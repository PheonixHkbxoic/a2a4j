package cn.pheker.ai.a2a4j.agent.autoconfiguration;


import cn.pheker.ai.a2a4j.core.core.PushNotificationSenderAuth;
import cn.pheker.ai.a2a4j.core.core.TaskManager;
import cn.pheker.ai.a2a4j.core.spec.entity.AgentCard;
import cn.pheker.ai.a2a4j.mvc.WebMvcSseServerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/19 00:21
 * @desc
 */
@ConditionalOnClass(value = {AgentCard.class, TaskManager.class})
@Configuration
public class A2a4jAgentAutoConfiguration {

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
