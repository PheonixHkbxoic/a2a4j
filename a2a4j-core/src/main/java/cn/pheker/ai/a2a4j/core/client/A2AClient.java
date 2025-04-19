package cn.pheker.ai.a2a4j.core.client;

import cn.pheker.ai.a2a4j.core.client.sse.SseEventReader;
import cn.pheker.ai.a2a4j.core.spec.entity.*;
import cn.pheker.ai.a2a4j.core.spec.error.A2AClientHTTPError;
import cn.pheker.ai.a2a4j.core.spec.error.A2AClientJSONError;
import cn.pheker.ai.a2a4j.core.spec.message.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/12 18:44
 * @desc
 */
@Getter
@Slf4j
public class A2AClient {
    private final AgentCard agentCard;
    private final PushNotificationConfig pushNotificationConfig;
    @Getter(AccessLevel.PROTECTED)
    private final transient CloseableHttpClient http;
    @Getter(AccessLevel.PROTECTED)
    private final transient ObjectMapper objectMapper = new ObjectMapper();


    public A2AClient(AgentCard agentCard) {
        this(agentCard, null);
    }

    public A2AClient(AgentCard agentCard, PushNotificationConfig pushNotificationConfig) {
        this.agentCard = agentCard;
        this.pushNotificationConfig = pushNotificationConfig;
        this.http = this.initHttpClient();
    }

    public void close() {
        try {
            this.http.close();
        } catch (IOException e) {
            log.error("client close failed: {}", e.getMessage(), e);
        }
    }

    public SendTaskResponse sendTask(TaskSendParams params) {
        SendTaskRequest request = new SendTaskRequest(params);
        Mono<JsonRpcResponse> responseMono = this.doSendRequest(request);
        return responseMono.map(r -> {
            TypeReference<SendTaskResponse> ref = new TypeReference<SendTaskResponse>() {
            };
            return objectMapper.convertValue(r, ref);
        }).block();
    }

    public GetTaskResponse getTask(TaskQueryParams params) {
        GetTaskRequest request = new GetTaskRequest(params);
        Mono<JsonRpcResponse> responseMono = this.doSendRequest(request);
        return responseMono.map(r -> {
            TypeReference<GetTaskResponse> ref = new TypeReference<GetTaskResponse>() {
            };
            return objectMapper.convertValue(r, ref);
        }).block();
    }

    public CancelTaskResponse cancelTask(TaskIdParams params) {
        CancelTaskRequest request = new CancelTaskRequest(params);
        Mono<JsonRpcResponse> responseMono = this.doSendRequest(request);
        return responseMono.map(r -> {
            TypeReference<CancelTaskResponse> ref = new TypeReference<CancelTaskResponse>() {
            };
            return objectMapper.convertValue(r, ref);
        }).block();
    }

    public Flux<SendTaskStreamingResponse> sendTaskSubscribe(TaskSendParams params) {
        SendTaskStreamingRequest request = new SendTaskStreamingRequest(params);
        return this.doSendRequestForSse(request);
    }

    public Flux<SendTaskStreamingResponse> sendTaskResubscribe(TaskIdParams params) {
        TaskResubscriptionRequest request = new TaskResubscriptionRequest(params);
        return this.doSendRequestForSse(request);
    }

    private Flux<SendTaskStreamingResponse> doSendRequestForSse(JsonRpcRequest request) {
        HttpPost httpPost = new HttpPost(agentCard.getUrl());
        try {
            StringEntity stringEntity = new StringEntity(objectMapper.writeValueAsString(request), StandardCharsets.UTF_8.name());
            stringEntity.setContentEncoding(StandardCharsets.UTF_8.name());
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);

            CloseableHttpResponse response = this.http.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            log.debug("doSendRequestForSse statusCode: {}", statusCode);
            if (HttpStatus.SC_OK != statusCode) {
                throw new A2AClientHTTPError(statusCode, response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            BufferedReader buf = new BufferedReader(new InputStreamReader(entity.getContent()));
            SseEventReader reader = new SseEventReader(buf);
            boolean isSse = entity.getContentType().getValue().contains("text/event-stream");
            log.debug("isSse: {}, contentType: {}", isSse, entity.getContentType().toString());
            if (isSse) {
                return Flux.<SendTaskStreamingResponse>create(sink -> {
                    reader.onEvent(sseEvent -> {
                        log.debug("doSendRequestForSse sseEvent: {}", sseEvent);
                        if (!sseEvent.hasData()) {
                            return;
                        }
                        SendTaskStreamingResponse sendTaskStreamingResponse;
                        try {
                            sendTaskStreamingResponse = objectMapper.readValue(sseEvent.getData(), SendTaskStreamingResponse.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        sink.next(sendTaskStreamingResponse);
                        UpdateEvent event = sendTaskStreamingResponse.getResult();
                        if (event instanceof TaskStatusUpdateEvent && ((TaskStatusUpdateEvent) event).isFinalFlag()) {
                            sink.complete();
                        }
                    }, e -> {
                        log.warn("doSendRequestForSse exception: {}", e.getMessage());
                        sink.error(e);
                    });
                }).doOnComplete(() -> {
                    try {
                        buf.close();
                        response.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // non sse
            String json = EntityUtils.toString(entity);
            SendTaskStreamingResponse sendTaskStreamingResponse = objectMapper.readValue(json, SendTaskStreamingResponse.class);
            return Flux.just(sendTaskStreamingResponse);
        } catch (JacksonException e) {
            return Flux.error(new A2AClientJSONError(e.getMessage()));
        } catch (Exception e) {
            return Flux.error(e);
        }
    }


    private Mono<JsonRpcResponse> doSendRequest(JsonRpcRequest request) {
        HttpPost httpPost = new HttpPost(agentCard.getUrl());
        try {
            StringEntity stringEntity = new StringEntity(objectMapper.writeValueAsString(request), StandardCharsets.UTF_8.name());
            stringEntity.setContentEncoding(StandardCharsets.UTF_8.name());
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);

            CloseableHttpResponse response = this.http.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK != statusCode) {
                throw new A2AClientHTTPError(statusCode, response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            JsonRpcResponse rpcResponse = objectMapper.readValue(result, JsonRpcResponse.class);
            return Mono.just(rpcResponse);
        } catch (JacksonException e) {
            throw new A2AClientJSONError(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private CloseableHttpClient initHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(30000).setConnectTimeout(30000).setConnectionRequestTimeout(5000)
                .build();
        HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(requestConfig);


        // enable ssl

        // enable proxy

        // and so on

        return builder.build();
    }

}
