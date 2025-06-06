package io.github.pheonixhkbxoic.a2a4j.notification.webflux.autoconfiguration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author PheonixHkbxoic
 */
@Data
@ConfigurationProperties(prefix = "a2a4j.notification", ignoreInvalidFields = true)
@Configuration(proxyBeanMethods = false)
public class A2a4jNotificationProperties {
    private String endpoint;
    private List<String> jwksUrls;
}
