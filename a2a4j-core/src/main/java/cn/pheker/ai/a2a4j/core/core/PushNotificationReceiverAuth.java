package cn.pheker.ai.a2a4j.core.core;

import cn.pheker.ai.a2a4j.core.spec.ValueError;
import cn.pheker.ai.a2a4j.core.util.Util;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/18 01:41
 * @desc
 */
@Slf4j
public class PushNotificationReceiverAuth extends PushNotificationAuth {
    private JWKSet jwkSet;

    /**
     * load multi jwk into JWKSet from jwksUrl list
     * ignore the jwk if it's keyID exists
     *
     * @param jwksUrls
     */
    public PushNotificationReceiverAuth loadJwks(List<String> jwksUrls) {
        Map<String, JWK> jwkMap = new HashMap<>(jwksUrls.size());
        if (jwkSet != null) {
            jwkSet.getKeys().forEach(jwk -> jwkMap.putIfAbsent(jwk.getKeyID(), jwk));
        }
        jwksUrls.stream().forEach(jwksUrl -> {
            try {
                JWKSet jwks = JWKSet.load(new URL(jwksUrl));
                jwks.getKeys().forEach(jwk -> jwkMap.putIfAbsent(jwk.getKeyID(), jwk));
            } catch (IOException | ParseException e) {
                log.error("loadJwks error: {}, url: {}", e.getMessage(), jwksUrl);
            }
        });

        if (jwkMap.isEmpty()) {
            throw new RuntimeException("a2a4j.notification.jwksUrls are all not unavailable");
        }
        this.jwkSet = new JWKSet(new ArrayList<>(jwkMap.values()));
        log.info("jwk keyID list: {}", jwkMap.keySet().stream().collect(Collectors.joining(",")));
        return this;
    }

    public PushNotificationReceiverAuth loadJwks(String... jwksUrls) {
        if (jwksUrls == null || jwksUrls.length == 0) {
            return this;
        }
        this.loadJwks(Arrays.stream(jwksUrls).collect(Collectors.toList()));
        return this;
    }

    public void loadJwksJson(String jwksJson) {
        try {
            this.jwkSet = JWKSet.parse(jwksJson);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean verifyPushNotification(String authorization, Object data) {
        if (Util.isEmpty(authorization) || !authorization.startsWith(AUTH_HEADER_PREFIX)) {
            log.info("Invalid authorization header: {}", authorization);
            return false;
        }

        String token = authorization.substring(AUTH_HEADER_PREFIX.length());

        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

            // 1. check request body signature
            String dataSha256 = claims.getStringClaim(REQUEST_BODY_SHA256_NAME);
            String live = this.sha256(data);
            if (Util.isEmpty(dataSha256) || !dataSha256.equals(live)) {
                throw new ValueError("Invalid request body");
            }

            // 2. check issue time is expired or not
            if (claims.getExpirationTime().before(new Date())) {
                throw new ValueError("Token is expired");
            }

            // 3. check jwt signature
            String keyId = signedJwt.getHeader().getKeyID();
            JWK jwk = jwkSet.getKeyByKeyId(keyId);
            if (jwk == null) {
                String availableKeyIdList = jwkSet.getKeys().stream().map(JWK::getKeyID).collect(Collectors.joining(","));
                String error = String.format("not found jwk by keyId: %s, available keyId list: %s", keyId, availableKeyIdList);
                throw new RuntimeException(error);
            }
            JWSVerifier verifier = new RSASSAVerifier(jwk.toRSAKey().toRSAPublicKey());
            return signedJwt.verify(verifier);
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.warn("verifyPushNotification failed: {}", e.getMessage());
            return false;
        }
    }

}
