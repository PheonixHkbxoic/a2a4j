package io.github.PheonixHkbxoic.a2a4j.mvc;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.PheonixHkbxoic.a2a4j.core.core.PushNotificationSenderAuth;
import io.github.PheonixHkbxoic.a2a4j.core.core.ServerAdapter;
import io.github.PheonixHkbxoic.a2a4j.core.core.TaskManager;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.InvalidRequestError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.JSONParseError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.MethodNotFoundError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.message.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@Data
@Slf4j
public class WebMvcSseServerAdapter implements ServerAdapter {
    private ObjectMapper om;
    private String messageEndpoint = "/";
    private String agentCardEndpoint = "/.well-known/agent.json";
    private String jwksEndpoint = "/.well-known/jwks.json";
    private final AgentCard agentCard;
    private final RouterFunction<ServerResponse> routerFunction;
    private TaskManager taskManager;
    private Validator validator;
    private PushNotificationSenderAuth auth;

    private volatile boolean isClosing = false;

    public WebMvcSseServerAdapter(AgentCard agentCard, TaskManager taskManager, Validator validator, PushNotificationSenderAuth auth) {
        this(new ObjectMapper(), null, null, agentCard, taskManager, validator, null, auth);
    }

    public WebMvcSseServerAdapter(ObjectMapper objectMapper, String messageEndpoint,
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
                .GET(this.jwksEndpoint, request -> {
                    Map<String, Object> data = Collections.singletonMap("keys", Collections.singletonList(auth.getPublicKey().toJSONObject()));
                    return ServerResponse.ok().body(data);
                })
                .POST(this.messageEndpoint, this::handleMessage)
                .build();
    }


    @Override
    public Mono<Void> closeGracefully() {
        this.isClosing = true;
        return this.taskManager.closeGracefully();
    }


    private ServerResponse handleAgentCard(ServerRequest request) {
        if (this.isClosing) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body("Server is shutting down");
        }

        try {
            return ServerResponse.ok().body(om.writeValueAsString(agentCard));
        } catch (Exception e) {
            log.error("Failed to get agent card: {}", e.getMessage());
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @SuppressWarnings("unchecked")
    private ServerResponse handleMessage(ServerRequest request) {
        if (this.isClosing) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body("Server is shutting down");
        }

        try {
            String body = request.body(String.class);
            JsonRpcRequest<Object> req = om.readValue(body, JsonRpcRequest.class);
            if (validator != null) {
                validator.validate(req);
            }

            // streaming request
            if (new SendTaskStreamingRequest().getMethod().equalsIgnoreCase(req.getMethod())
                    || new TaskResubscriptionRequest().getMethod().equalsIgnoreCase(req.getMethod())) {
                return this.handleRequestSse(req);
            }

            // common request
            return handleRequest(req);
        } catch (JacksonException e) {
            log.error("json parse error: {}", e.getMessage(), e);
            return ServerResponse.badRequest().body(new JSONParseError());
        } catch (IllegalArgumentException | IllegalStateException | ValidationException e) {
            log.error("invalid request error: {}", e.getMessage(), e);
            return ServerResponse.badRequest().body(new InvalidRequestError());
        } catch (Exception e) {
            log.error("handle request error: {}", e.getMessage(), e);
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new InternalError());
        }
    }

    public <T> ServerResponse handleRequestSse(JsonRpcRequest<T> request) {
        Mono<JsonRpcResponse> monoResponse;
        String taskId;
        if (new SendTaskStreamingRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            SendTaskStreamingRequest req = om.convertValue(request, new TypeReference<SendTaskStreamingRequest>() {
            });
            monoResponse = taskManager.onSendTaskSubscribe(req);
            taskId = req.getParams().getId();
        } else if (new TaskResubscriptionRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            TaskResubscriptionRequest req = om.convertValue(request, new TypeReference<TaskResubscriptionRequest>() {
            });
            monoResponse = taskManager.onResubscribeTask(req);
            taskId = req.getParams().getId();
        } else {
            monoResponse = Mono.just(new JsonRpcResponse<>(request.getId(), new MethodNotFoundError()));
            taskId = "";
        }

        // exception return rpcResponse
        JsonRpcResponse rpcResponse = monoResponse.block();
        if (rpcResponse != null) {
            return ServerResponse.ok().body(rpcResponse);
        }

        // sse response
        return ServerResponse.sse(sseBuilder -> {
            sseBuilder.onComplete(() -> {
                log.debug("SSE connection completed for request: {}", request.getId());
            });
            sseBuilder.onTimeout(() -> {
                log.debug("SSE connection timed out for request: {}", request.getId());
            });


            // sse handle
            taskManager.dequeueEvent(taskId)
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnError(sseBuilder::error)
                    .subscribe(updateEvent -> {
                        log.debug("dequeueEvent taskId: {}, updateEvent: {}", taskId, updateEvent);
                        this.sendMessage(sseBuilder, taskId, new SendTaskStreamingResponse(request.getId(), updateEvent));
                    });
        });
    }

    public void sendMessage(ServerResponse.SseBuilder sseBuilder, String sessionId, JsonRpcMessage message) {
        try {
            String jsonText = om.writeValueAsString(message);
            log.debug("sendMessage: {}", jsonText);
            sseBuilder.id(sessionId).event("message").data(jsonText);
        } catch (Exception e) {
            log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
            sseBuilder.error(e);
        }
    }

    public <T> ServerResponse handleRequest(JsonRpcRequest<T> request) {
        JsonRpcResponse response;
        if (new GetTaskRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            GetTaskRequest req = om.convertValue(request, new TypeReference<GetTaskRequest>() {
            });
            response = taskManager.onGetTask(req);
        } else if (new SendTaskRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            SendTaskRequest req = om.convertValue(request, new TypeReference<SendTaskRequest>() {
            });
            response = taskManager.onSendTask(req);
        } else if (new CancelTaskRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            CancelTaskRequest req = om.convertValue(request, new TypeReference<CancelTaskRequest>() {
            });
            response = taskManager.onCancelTask(req);
        } else {
            response = new JsonRpcResponse(request.getId(), new MethodNotFoundError());
        }
        return ServerResponse.ok().body(response);
    }


}


