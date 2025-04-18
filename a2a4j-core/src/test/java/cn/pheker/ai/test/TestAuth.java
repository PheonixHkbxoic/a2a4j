package cn.pheker.ai.test;

import cn.pheker.ai.core.PushNotificationAuth;
import cn.pheker.ai.core.PushNotificationReceiverAuth;
import cn.pheker.ai.core.PushNotificationSenderAuth;
import cn.pheker.ai.util.Util;
import com.nimbusds.jose.jwk.JWK;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/18 01:09
 * @desc
 */
@Slf4j
public class TestAuth {
    @Test
    public void TestPushNotificationAuth() {
        // sender generate jwks
        PushNotificationSenderAuth senderAuth = new PushNotificationSenderAuth();
        JWK jwk = senderAuth.getJwk();
        JWK publicKey = senderAuth.getPublicKey();
        Assertions.assertThat(jwk).isNotNull();
        Assertions.assertThat(publicKey).isNotNull();

        Map<String, String> data = Collections.singletonMap("test", "well done");
        String token = senderAuth.generateJwt(data);
        Assertions.assertThat(token).isNotNull();
        log.info("privateKey: {}", jwk);
        log.info("publicKey: {}", publicKey);
        log.info("jwt token: {}", token);

        Map<String, List<Map<String, Object>>> jwks = Collections.singletonMap("keys", Collections.singletonList(publicKey.toJSONObject()));
        String jwksJson = Util.json(jwks);
        log.info("jwks.json: {}", jwksJson);

        // receiver load jwks and verify token and data
        PushNotificationReceiverAuth receiverAuth = new PushNotificationReceiverAuth();
        receiverAuth.loadJwksJson(jwksJson);
        boolean verified = receiverAuth.verifyPushNotification(PushNotificationAuth.AUTH_HEADER_PREFIX + token, data);
        Assertions.assertThat(verified).isTrue();
    }

}
