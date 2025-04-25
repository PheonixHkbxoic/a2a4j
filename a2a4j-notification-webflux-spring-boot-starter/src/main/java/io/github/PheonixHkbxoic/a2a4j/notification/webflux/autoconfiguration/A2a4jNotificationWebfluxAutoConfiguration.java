package io.github.PheonixHkbxoic.a2a4j.notification.webflux.autoconfiguration;

import io.github.PheonixHkbxoic.a2a4j.webflux.WebfluxSseServerAdapter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author PheonixHkbxoic
 */
@EnableConfigurationProperties(A2a4jNotificationProperties.class)
@Configuration(proxyBeanMethods = false)
public class A2a4jNotificationWebfluxAutoConfiguration {

    @Bean
    public RouterFunction<ServerResponse> routerFunction(WebfluxSseServerAdapter webFluxSseServerAdapter) {
        return webFluxSseServerAdapter.getRouterFunction();
    }

}
