package io.github.pheonixhkbxoic.a2a4j.webflux;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pheonixhkbxoic.a2a4j.core.core.PushNotificationSenderAuth;
import io.github.pheonixhkbxoic.a2a4j.core.core.ServerAdapter;
import io.github.pheonixhkbxoic.a2a4j.core.core.TaskManager;
import io.github.pheonixhkbxoic.a2a4j.core.server.A2AServer;
import io.github.pheonixhkbxoic.a2a4j.core.spec.Method;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.InvalidRequestError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.JSONParseError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.MethodNotFoundError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.*;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@Data
@Slf4j
public class WebfluxSseServerAdapter implements ServerAdapter {
    private ObjectMapper om;
    private String messageEndpoint = "/";
    private String agentCardEndpoint = "/.well-known/agent.json";
    private String jwksEndpoint = "/.well-known/jwks.json";
    private final AgentCard agentCard;
    private final RouterFunction<ServerResponse> routerFunction;
    private TaskManager taskManager;
    private Validator validator;
    private PushNotificationSenderAuth auth;

    private volatile boolean isClosing = true;
    private final A2AServer server;

    public WebfluxSseServerAdapter(AgentCard agentCard, TaskManager taskManager, Validator validator, PushNotificationSenderAuth auth) {
        this(new ObjectMapper(), null, null, agentCard, taskManager, validator, null, auth);
    }

    public WebfluxSseServerAdapter(ObjectMapper objectMapper, String messageEndpoint,
                                   String agentCardEndpoint, AgentCard agentCard, TaskManager taskManager,
                                   Validator validator, String jwksEndpoint, PushNotificationSenderAuth auth) {
        om = objectMapper;
        if (messageEndpoint != null) {
            this.messageEndpoint = messageEndpoint;
        }
        if (agentCardEndpoint != null) {
            this.agentCardEndpoint = agentCardEndpoint;
        }
        if (jwksEndpoint != null) {
            this.jwksEndpoint = jwksEndpoint;
        }
        this.agentCard = agentCard;
        this.taskManager = taskManager;
        this.validator = validator;
        this.auth = auth;
        this.routerFunction = RouterFunctions.route()
                .GET(this.agentCardEndpoint, this::handleAgentCard)
                .GET(this.jwksEndpoint, this::handleJwks)
                .POST(this.messageEndpoint, this::handleMessage)
                .build();
        this.server = new A2AServer(this.agentCard, this);
    }


    @Override
    public Mono<Void> closeGracefully() {
        this.isClosing = true;
        return this.taskManager.closeGracefully();
    }

    @Override
    public void start() {
        this.isClosing = false;
    }

    @Override
    public A2AServer getServer() {
        return this.server;
    }


    private Mono<ServerResponse> handleAgentCard(ServerRequest request) {
        if (this.isClosing) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).bodyValue("Server is shutting down");
        }

        return request.bodyToMono(String.class)
                .switchIfEmpty(Mono.just(""))
                .flatMap(body -> ServerResponse.ok().bodyValue(agentCard));
    }

    private Mono<ServerResponse> handleJwks(ServerRequest request) {
        if (this.isClosing) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).bodyValue("Server is shutting down");
        }
        return request.bodyToMono(String.class)
                .switchIfEmpty(Mono.just(""))
                .flatMap(body -> {
                    Map<String, Object> data = Collections.singletonMap("keys", Collections.singletonList(auth.getPublicKey().toJSONObject()));
                    return ServerResponse.ok().bodyValue(data);
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<ServerResponse> handleMessage(ServerRequest request) {
        if (this.isClosing) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).bodyValue("Server is shutting down");
        }

        return request.bodyToMono(String.class)
                .switchIfEmpty(Mono.just(""))
                .flatMap(body -> {
                    try {
                        if (body.isEmpty()) {
                            throw new IllegalArgumentException("body is empty");
                        }
                        JsonRpcRequest<Object> req = om.readValue(body, JsonRpcRequest.class);

                        // streaming request
                        if (new SendTaskStreamingRequest().getMethod().equalsIgnoreCase(req.getMethod())
                                || new TaskResubscriptionRequest().getMethod().equalsIgnoreCase(req.getMethod())) {
                            return this.handleRequestSse(req);
                        }

                        // common request
                        return handleRequest(req);
                    } catch (JacksonException e) {
                        log.error("json parse error: {}", e.getMessage(), e);
                        return ServerResponse.badRequest().bodyValue(new JSONParseError());
                    } catch (IllegalArgumentException | IllegalStateException | ValidationException e) {
                        log.error("invalid request error: {}", e.getMessage(), e);
                        return ServerResponse.badRequest().bodyValue(new InvalidRequestError());
                    } catch (Exception e) {
                        log.error("handle request error: {}", e.getMessage(), e);
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(new InternalError());
                    }

                });
    }

    public <T> Mono<ServerResponse> handleRequestSse(JsonRpcRequest<T> request) {
        Mono<? extends JsonRpcResponse<?>> monoResponse;
        String taskId;
        if (Method.TASKS_SENDSUBSCRIBE.equalsIgnoreCase(request.getMethod())) {
            SendTaskStreamingRequest req = om.convertValue(request, new TypeReference<>() {
            });
            Util.validate(validator, req);
            monoResponse = taskManager.onSendTaskSubscribe(req);
            taskId = req.getParams().getId();
        } else if (Method.TASKS_RESUBSCRIBE.equalsIgnoreCase(request.getMethod())) {
            TaskResubscriptionRequest req = om.convertValue(request, new TypeReference<>() {
            });
            Util.validate(validator, req);
            monoResponse = taskManager.onResubscribeTask(req);
            taskId = req.getParams().getId();
        } else {
            return ServerResponse.badRequest().bodyValue(new MethodNotFoundError());
        }

        return monoResponse
                // business error return status ok
                .flatMap(error -> ServerResponse.ok().bodyValue(error))
                // when monoResponse is empty
                // return sse response
                .switchIfEmpty(ServerResponse.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(Flux.<ServerSentEvent<JsonRpcResponse<?>>>create(sink -> {
                            sink.onDispose(() -> log.debug("SSE connection completed for request: {}", request.getId()));
                            sink.onCancel(() -> log.debug("SSE connection timed out for request: {}", request.getId()));


                            // sse handle
                            taskManager.dequeueEvent(taskId)
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .doOnError(sink::error)
                                    .doOnComplete(sink::complete)
                                    .subscribe(updateEvent -> {
                                        log.debug("dequeueEvent taskId: {}, updateEvent: {}", taskId, updateEvent);
                                        this.sendMessage(sink, taskId, new SendTaskStreamingResponse(request.getId(), updateEvent));
                                    });
                        }), ServerSentEvent.class));
    }

    public void sendMessage(FluxSink<ServerSentEvent<JsonRpcResponse<?>>> sink, String sessionId, JsonRpcResponse<?> message) {
        try {
            sink.next(ServerSentEvent.<JsonRpcResponse<?>>builder().id(sessionId).event(EVENT_MESSAGE).data(message).build());
        } catch (Exception e) {
            log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
            sink.error(e);
        }
    }

    public <T> Mono<ServerResponse> handleRequest(JsonRpcRequest<T> request) {
        Mono<? extends JsonRpcResponse<?>> response;
        if (Method.TASKS_GET.equalsIgnoreCase(request.getMethod())) {
            GetTaskRequest req = om.convertValue(request, new TypeReference<>() {
            });
            Util.validate(validator, req);
            response = taskManager.onGetTask(req);
        } else if (Method.TASKS_SEND.equalsIgnoreCase(request.getMethod())) {
            SendTaskRequest req = om.convertValue(request, new TypeReference<>() {
            });
            Util.validate(validator, req);
            response = taskManager.onSendTask(req);
        } else if (Method.TASKS_CANCEL.equalsIgnoreCase(request.getMethod())) {
            CancelTaskRequest req = om.convertValue(request, new TypeReference<>() {
            });
            Util.validate(validator, req);
            response = taskManager.onCancelTask(req);
        } else {
            return ServerResponse.badRequest().bodyValue(new MethodNotFoundError());
        }
        return ServerResponse.ok().body(response, JsonRpcResponse.class);
    }


}


