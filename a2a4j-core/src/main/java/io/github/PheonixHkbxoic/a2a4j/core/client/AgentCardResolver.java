package io.github.PheonixHkbxoic.a2a4j.core.client;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.PheonixHkbxoic.a2a4j.core.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

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
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            httpGet.setConfig(requestConfig);
            CloseableHttpResponse response = http.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK != statusCode) {
                httpGet.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }

            HttpEntity entity = response.getEntity();
            if (null != entity) {
                String result = EntityUtils.toString(entity);
                log.debug("agent card json: {}", result);
                return new ObjectMapper().readValue(result, AgentCard.class);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
