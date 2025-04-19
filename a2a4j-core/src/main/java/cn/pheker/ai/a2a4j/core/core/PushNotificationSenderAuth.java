package cn.pheker.ai.a2a4j.core.core;

import cn.pheker.ai.a2a4j.core.util.Uuid;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/17 23:51
 * @desc
 */
@Slf4j
public class PushNotificationSenderAuth extends PushNotificationAuth {
    @Getter
    private RSAKey jwk;
    @Getter
    private JWK publicKey;

    public PushNotificationSenderAuth() {
        this.generateJwk();
    }

    private void generateJwk() {
        try {
            this.jwk = new RSAKeyGenerator(2048)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(UUID.randomUUID().toString())
                    .issueTime(new Date())
                    .generate();
            this.publicKey = jwk.toPublicJWK();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * generate jwt token by request body data
     *
     * @param data request body data, it's entity not json string of entity
     * @return jwt token
     */
    public String generateJwt(Object data) {
        try {
            // sign with private key, but put the keyId of publicKey into header
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(publicKey.getKeyID()).build();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject("A2AServer")
                    .issueTime(new Date())
                    .issuer("cn.pheker")
                    .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                    .claim(REQUEST_BODY_SHA256_NAME, this.sha256(data))
                    .build();
            SignedJWT signedJwt = new SignedJWT(header, claimsSet);

            JWSSigner signer = new RSASSASigner(jwk);
            signedJwt.sign(signer);
            return signedJwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPushNotification(String url, Object data) {
        String token = null;
        CloseableHttpResponse response;
        try (CloseableHttpClient http = HttpClients.createDefault()) {
            token = this.generateJwt(data);

            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader(AUTH_HEADER, AUTH_HEADER_PREFIX + token);

            String json = objectMapper.writeValueAsString(data);
            StringEntity stringEntity = new StringEntity(json, StandardCharsets.UTF_8.name());
            stringEntity.setContentEncoding(StandardCharsets.UTF_8.name());
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);

            response = http.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(response.getStatusLine().getReasonPhrase());
            }
//            log.info("Push-notification sent for URL: {}, token: {}, data: {}", url, token, json);
            log.info("Push-notification sent for URL: {}", url);
        } catch (IOException e) {
            log.warn("Error during sending push-notification for URL: {}, token: {}", url, token);
        }
    }

    public static boolean verifyPushNotificationUrl(String url) {
        CloseableHttpResponse response;
        try (CloseableHttpClient http = HttpClients.createDefault()) {
            String validationToken = Uuid.uuid4hex();
            HttpGet request = new HttpGet(url + "?validationToken=" + validationToken);
            response = http.execute(request);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(response.getStatusLine().getReasonPhrase());
            }
            String content = EntityUtils.toString(response.getEntity());
            boolean isVerified = validationToken.equals(content);
            log.info("Verified push-notification URL: {} => {}", url, isVerified);
            return isVerified;
        } catch (Exception e) {
            log.warn("Error during sending push-notification for URL {}: {}", url, e.getMessage());
        }
        return false;
    }

}
