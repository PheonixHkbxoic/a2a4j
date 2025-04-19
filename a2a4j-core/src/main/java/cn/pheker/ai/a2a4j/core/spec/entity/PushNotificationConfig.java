package cn.pheker.ai.a2a4j.core.spec.entity;

import cn.pheker.ai.a2a4j.core.spec.Nullable;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 23:48
 * @desc
 */
@ToString
@Data
public class PushNotificationConfig implements Serializable {
    private String url;
    @Nullable
    private String token;
    @Nullable
    private AuthenticationInfo authentication;
}
