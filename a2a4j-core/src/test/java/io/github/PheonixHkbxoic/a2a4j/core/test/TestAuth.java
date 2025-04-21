package io.github.PheonixHkbxoic.a2a4j.core.test;

import io.github.PheonixHkbxoic.a2a4j.core.core.PushNotificationAuth;
import io.github.PheonixHkbxoic.a2a4j.core.core.PushNotificationReceiverAuth;
import io.github.PheonixHkbxoic.a2a4j.core.core.PushNotificationSenderAuth;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.TextPart;
import io.github.PheonixHkbxoic.a2a4j.core.util.Util;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author PheonixHkbxoic
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

        TextPart textPart = new TextPart("hello");
        String token = senderAuth.generateJwt(textPart);
        Assertions.assertThat(token).isNotNull();
        log.info("privateKey: {}, keyId: {}", jwk, jwk.getKeyID());
        log.info("publicKey: {}, keyId: {}", publicKey, publicKey.getKeyID());
        log.info("privateKey keyId == publicKey keyId: {}", jwk.getKeyID().equals(publicKey.getKeyID()));
        log.info("jwt token: {}", token);

        Map<String, List<Map<String, Object>>> jwks = Collections.singletonMap("keys", Collections.singletonList(publicKey.toJSONObject()));
        String jwksJson = Util.toJson(jwks);
        log.info("jwks.json: {}", jwksJson);

        // receiver load jwks and verify token and data
        String json = Util.toJson(textPart);
        log.info("json: {}", json);
        TextPart dataMap = Util.fromJson(json, TextPart.class);
        PushNotificationReceiverAuth receiverAuth = new PushNotificationReceiverAuth();
        receiverAuth.loadJwksJson(jwksJson);
        boolean verified = receiverAuth.verifyPushNotification(PushNotificationAuth.AUTH_HEADER_PREFIX + token, dataMap);
        Assertions.assertThat(verified).isTrue();

        try {
            SignedJWT jwt = SignedJWT.parse(token);
            String keyIdInToken = jwt.getHeader().getKeyID();
            log.info("keyIdInToken: {}, == publicKey keyId: {}", keyIdInToken, keyIdInToken.equals(publicKey.getKeyID()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
