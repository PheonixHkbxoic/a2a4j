package io.github.pheonixhkbxoic.a2a4j.core.client;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pheonixhkbxoic.a2a4j.core.client.sse.SseEventReader;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.A2AClientError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.A2AClientHTTPError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.A2AClientJSONError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.*;
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
 * @desc a2a client
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

    public Mono<SendTaskResponse> sendTask(TaskSendParams params) {
        SendTaskRequest request = new SendTaskRequest(params);
        return this.doSendRequest(request).map(r -> objectMapper.convertValue(r, new TypeReference<>() {
        }));
    }

    public Mono<GetTaskResponse> getTask(TaskQueryParams params) {
        GetTaskRequest request = new GetTaskRequest(params);
        return this.doSendRequest(request).map(r -> objectMapper.convertValue(r, new TypeReference<>() {
        }));
    }

    public Mono<CancelTaskResponse> cancelTask(TaskIdParams params) {
        CancelTaskRequest request = new CancelTaskRequest(params);
        return this.doSendRequest(request).map(r -> objectMapper.convertValue(r, new TypeReference<>() {
        }));
    }

    public Flux<SendTaskStreamingResponse> sendTaskSubscribe(TaskSendParams params) {
        SendTaskStreamingRequest request = new SendTaskStreamingRequest(params);
        return this.doSendRequestForSse(request);
    }

    public Flux<SendTaskStreamingResponse> sendTaskResubscribe(TaskIdParams params) {
        TaskResubscriptionRequest request = new TaskResubscriptionRequest(params);
        return this.doSendRequestForSse(request);
    }

    private Flux<SendTaskStreamingResponse> doSendRequestForSse(JsonRpcRequest<?> request) {
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
            log.debug("isSse: {}, contentType: {}", isSse, entity.getContentType());
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
                            }, sink::error);
                        })
                        .doOnError(e -> {
                            log.warn("doSendRequestForSse exception: {}", e.getMessage());
                            try {
                                buf.close();
                                response.close();
                                EntityUtils.consume(entity);
                            } catch (IOException ignored) {

                            }
                        })
                        .doOnComplete(() -> {
                            try {
                                buf.close();
                                response.close();
                                EntityUtils.consume(entity);
                            } catch (IOException ignored) {

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


    private Mono<JsonRpcResponse<?>> doSendRequest(JsonRpcRequest<?> request) {
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
            JsonRpcResponse<?> rpcResponse = objectMapper.readValue(result, JsonRpcResponse.class);
            return Mono.just(rpcResponse);
        } catch (JacksonException e) {
            throw new A2AClientJSONError(e.getMessage());
        } catch (A2AClientError e) {
            throw e;
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
