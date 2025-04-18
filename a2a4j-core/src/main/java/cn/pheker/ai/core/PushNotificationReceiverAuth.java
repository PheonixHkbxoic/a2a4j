package cn.pheker.ai.core;

import cn.pheker.ai.spec.ValueError;
import cn.pheker.ai.util.Util;
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
import java.util.Date;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/18 01:41
 * @desc
 */
@Slf4j
public class PushNotificationReceiverAuth extends PushNotificationAuth {
    private JWKSet jwkSet;

    public void loadJwks(String jwksUrl) {
        try {
            this.jwkSet = JWKSet.load(new URL(jwksUrl));
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
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
            JWK jwk = jwkSet.getKeys().get(0);
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
