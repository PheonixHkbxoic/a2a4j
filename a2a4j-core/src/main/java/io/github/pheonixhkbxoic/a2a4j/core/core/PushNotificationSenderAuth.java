package io.github.pheonixhkbxoic.a2a4j.core.core;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import io.github.pheonixhkbxoic.a2a4j.core.util.Uuid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author PheonixHkbxoic
 */
@Slf4j
public class PushNotificationSenderAuth extends PushNotificationAuth {
    @Getter
    private RSAKey jwk;
    @Getter
    private JWK publicKey;
    private static CloseableHttpAsyncClient http;

    public PushNotificationSenderAuth() {
        this.generateJwk();
        http = initHttpClient();
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
        final String token = this.generateJwt(data);
        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("Error during sending push-notification for URL: {}, token: {}, error: {}", url, token, e.getMessage(), e);
            return;
        }

        SimpleHttpRequest request = SimpleRequestBuilder.post(url)
                .addHeader(AUTH_HEADER, AUTH_HEADER_PREFIX + token)
                .setBody(json, ContentType.APPLICATION_JSON)
                .build();
        http.execute(request, new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse simpleHttpResponse) {
                log.info("Push-notification sent for URL: {}", url);
            }

            @Override
            public void failed(Exception e) {
                log.warn("Error during sending push-notification for URL: {}, token: {}", url, token);
            }

            @Override
            public void cancelled() {

            }
        });
    }

    public static boolean verifyPushNotificationUrl(String url) {
        try {
            String validationToken = Uuid.uuid4hex();
            SimpleHttpRequest request = SimpleRequestBuilder.get(url + "?validationToken=" + validationToken).build();
            Future<SimpleHttpResponse> future = http.execute(request, null);
            SimpleHttpResponse response = future.get();
            if (HttpStatus.SC_OK != response.getCode()) {
                log.error("Error during receiving push-notification for URL {}, code: {}, error: {}",
                        url, response.getCode(), response.getReasonPhrase());
                return false;
            }
            String content = response.getBodyText();
            boolean isVerified = validationToken.equals(content);
            log.info("Verified push-notification URL: {} => {}", url, isVerified);
            return isVerified;
        } catch (Exception e) {
            log.warn("Error during sending push-notification for URL {}: {}", url, e.getMessage());
        }
        return false;
    }

    private static CloseableHttpAsyncClient initHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(30000).setConnectTimeout(30000).setConnectionRequestTimeout(5000)
                .build();
        HttpAsyncClientBuilder builder = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig);


        // enable ssl

        // enable proxy

        // and so on

        CloseableHttpAsyncClient client = builder.build();
        client.start();
        return client;
    }

}
