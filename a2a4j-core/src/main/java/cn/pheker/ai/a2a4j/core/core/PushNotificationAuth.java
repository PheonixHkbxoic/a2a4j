package cn.pheker.ai.a2a4j.core.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/17 23:36
 * @desc
 */
public class PushNotificationAuth {
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_HEADER_PREFIX = "Bearer ";
    public static final String REQUEST_BODY_SHA256_NAME = "request_body_sha256";
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * calculate your entity obj' sha256
     *
     * @param obj your entity, not json string of your entity
     * @return sha256
     */
    protected String sha256(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] cipherBytes = messageDigest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : cipherBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static String sha256(String plainString) {
        String cipherString = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] cipherBytes = messageDigest.digest(plainString.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : cipherBytes) {
                sb.append(String.format("%02x", b));
            }
            cipherString = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherString;
    }


}
