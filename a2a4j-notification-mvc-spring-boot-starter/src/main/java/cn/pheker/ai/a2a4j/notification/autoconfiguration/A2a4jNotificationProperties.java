package cn.pheker.ai.a2a4j.notification.autoconfiguration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/19 15:32
 * @desc
 */
@Data
@ConfigurationProperties(prefix = "a2a4j.notification", ignoreInvalidFields = true)
@Configuration(proxyBeanMethods = false)
public class A2a4jNotificationProperties {
    private String endpoint;
    private List<String> jwksUrls;
}
