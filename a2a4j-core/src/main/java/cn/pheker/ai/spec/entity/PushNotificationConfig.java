package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import lombok.Data;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 23:48
 * @desc
 */
@Data
public class PushNotificationConfig implements Serializable {
    private String url;
    @Nullable
    private String token;
    @Nullable
    private AuthenticationInfo authentication;
}
