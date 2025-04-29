package io.github.pheonixhkbxoic.a2a4j.core.client;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pheonixhkbxoic.a2a4j.core.client.sse.SseEventReader;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.A2AClientHTTPError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.A2AClientJSONError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

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
    private final transient CloseableHttpAsyncClient http;
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
        try {
            String body = objectMapper.writeValueAsString(request);

            AsyncRequestProducer requestProducer = AsyncRequestBuilder.post(agentCard.getUrl())
                    .setEntity(body, ContentType.APPLICATION_JSON)
                    .build();
            Future<SimpleHttpResponse> future = this.http.execute(requestProducer, SimpleResponseConsumer.create(), null);
            SimpleHttpResponse simpleHttpResponse = future.get();
            boolean isSse = simpleHttpResponse.getContentType().getMimeType().contains("text/event-stream");
            if (isSse) {
                ByteArrayInputStream bais = new ByteArrayInputStream(simpleHttpResponse.getBodyBytes());
                BufferedReader buf = new BufferedReader(new InputStreamReader(bais));
                SseEventReader reader = new SseEventReader(buf);
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
                });
            }

            int statusCode = simpleHttpResponse.getCode();
            if (HttpStatus.SC_OK != statusCode) {
                String inValidRequestParam = new String(simpleHttpResponse.getBodyBytes(), StandardCharsets.UTF_8);
                return Flux.error(new A2AClientHTTPError(statusCode, inValidRequestParam));
            }
            // non sse
            String json = simpleHttpResponse.getBodyText();
            SendTaskStreamingResponse sendTaskStreamingResponse = objectMapper.readValue(json, SendTaskStreamingResponse.class);
            return Flux.just(sendTaskStreamingResponse);
        } catch (JacksonException e) {
            return Flux.error(new A2AClientJSONError(e.getMessage()));
        } catch (Exception e) {
            return Flux.error(e);
        }
    }


    private Mono<JsonRpcResponse<?>> doSendRequest(JsonRpcRequest<?> request) {
        return Mono.create(sink -> {
            String body;
            try {
                body = objectMapper.writeValueAsString(request);
            } catch (JacksonException e) {
                sink.error(e);
                return;
            }

            SimpleHttpRequest simpleHttpRequest = SimpleRequestBuilder.post(agentCard.getUrl())
                    .setBody(body, ContentType.APPLICATION_JSON)
                    .build();
            this.http.execute(simpleHttpRequest, new FutureCallback<>() {
                @Override
                public void completed(SimpleHttpResponse response) {
                    int statusCode = response.getCode();
                    if (HttpStatus.SC_OK != statusCode) {
                        String body = new String(response.getBodyBytes(), StandardCharsets.UTF_8);
                        sink.error(new A2AClientHTTPError(statusCode, body));
                        return;
                    }
                    try {
                        String result = new String(response.getBodyBytes(), StandardCharsets.UTF_8);
                        JsonRpcResponse<?> rpcResponse = objectMapper.readValue(result, JsonRpcResponse.class);
                        sink.success(rpcResponse);
                    } catch (JacksonException e) {
                        sink.error(e);
                    }
                }

                @Override
                public void failed(Exception e) {
                    sink.error(e.getCause());
                }

                @Override
                public void cancelled() {
                    sink.success();
                }
            });
        });
    }


    private CloseableHttpAsyncClient initHttpClient() {
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
