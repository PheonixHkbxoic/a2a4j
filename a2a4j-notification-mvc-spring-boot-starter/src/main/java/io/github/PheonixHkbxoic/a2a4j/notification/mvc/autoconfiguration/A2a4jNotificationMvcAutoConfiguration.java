package io.github.pheonixhkbxoic.a2a4j.notification.mvc.autoconfiguration;

import io.github.pheonixhkbxoic.a2a4j.mvc.WebMvcNotificationAdapter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author PheonixHkbxoic
 */
@EnableConfigurationProperties(io.github.pheonixhkbxoic.a2a4j.notification.mvc.autoconfiguration.A2a4jNotificationProperties.class)
@Configuration(proxyBeanMethods = false)
public class A2a4jNotificationMvcAutoConfiguration {

    @Bean
    public RouterFunction<ServerResponse> routerFunction(WebMvcNotificationAdapter webMvcNotificationAdapter) {
        return webMvcNotificationAdapter.getRouterFunction();
    }

}
