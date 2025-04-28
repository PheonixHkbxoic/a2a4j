package io.github.pheonixhkbxoic.a2a4j.core.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author PheonixHkbxoic
 */
@Getter
@Slf4j
public class AgentCardResolver {
    private final String name;
    private final String baseUrl;
    private String agentCardPath = "/.well-known/agent.json";

    public AgentCardResolver(String baseUrl) {
        this.name = null;
        this.baseUrl = baseUrl;
    }

    public AgentCardResolver(String name, String baseUrl, String agentCardPath) {
        this.name = name;
        this.baseUrl = baseUrl;
        if (!Util.isEmpty(agentCardPath)) {
            this.agentCardPath = agentCardPath;
        }
    }

    public AgentCard resolve() {
        String agentCardUrl = baseUrl.replaceAll("/+$", "") + "/" + agentCardPath.replaceFirst("^/+", "");
        log.debug("agent card url: {}", agentCardUrl);

        try (CloseableHttpClient http = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(agentCardUrl);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(20000, TimeUnit.MILLISECONDS)
                    .setResponseTimeout(20000, TimeUnit.MILLISECONDS)
                    .build();
            httpGet.setConfig(requestConfig);
            httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            String json = http.execute(httpGet, classicHttpResponse -> {
                int statusCode = classicHttpResponse.getCode();
                if (HttpStatus.SC_OK != statusCode) {
                    httpGet.abort();
                    throw new RuntimeException("HttpClient,error status code :" + statusCode);
                }

                HttpEntity entity = classicHttpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                log.debug("agent card json: {}", result);
                return result;
            });
            return new ObjectMapper().readValue(json, AgentCard.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
