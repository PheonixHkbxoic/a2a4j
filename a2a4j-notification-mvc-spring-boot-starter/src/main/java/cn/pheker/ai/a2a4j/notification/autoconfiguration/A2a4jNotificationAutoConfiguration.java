package cn.pheker.ai.a2a4j.notification.autoconfiguration;

import cn.pheker.ai.a2a4j.mvc.WebMvcNotificationAdapter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/19 15:26
 * @desc
 */
@EnableConfigurationProperties(A2a4jNotificationProperties.class)
@Configuration(proxyBeanMethods = false)
public class A2a4jNotificationAutoConfiguration {

    @Bean
    public RouterFunction<ServerResponse> routerFunction(WebMvcNotificationAdapter webMvcNotificationAdapter) {
        return webMvcNotificationAdapter.getRouterFunction();
    }

}
