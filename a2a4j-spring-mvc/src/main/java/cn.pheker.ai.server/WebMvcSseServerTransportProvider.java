package cn.pheker.ai.server;

import cn.pheker.ai.core.ServerSession;
import cn.pheker.ai.core.ServerTransport;
import cn.pheker.ai.core.ServerTransportProvider;
import cn.pheker.ai.core.TaskManager;
import cn.pheker.ai.spec.entity.AgentCard;
import cn.pheker.ai.spec.error.InvalidRequestError;
import cn.pheker.ai.spec.error.JSONParseError;
import cn.pheker.ai.spec.message.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


@Data
@Slf4j
public class WebMvcSseServerTransportProvider implements ServerTransportProvider {
    private static ObjectMapper OM;
    private String messageEndpoint = "/";
    private String agentCardEndpoint = "/.well-known/agent.json";
    private final AgentCard agentCard;
    private final RouterFunction<ServerResponse> routerFunction;
    private ServerSession.Factory sessionFactory;
    private final ConcurrentHashMap<String, ServerSession> sessions = new ConcurrentHashMap<>();
    private TaskManager taskManager;

    private volatile boolean isClosing = false;

    public WebMvcSseServerTransportProvider(AgentCard agentCard, TaskManager taskManager) {
        this(new ObjectMapper(), null, null, agentCard, taskManager);
    }

    public WebMvcSseServerTransportProvider(ObjectMapper objectMapper, String messageEndpoint,
                                            String agentCardEndpoint, AgentCard agentCard, TaskManager taskManager) {
        OM = objectMapper;
        if (messageEndpoint != null) {
            this.messageEndpoint = messageEndpoint;
        }
        if (agentCardEndpoint != null) {
            this.agentCardEndpoint = agentCardEndpoint;
        }
        this.agentCard = agentCard;
        this.taskManager = taskManager;
        this.routerFunction = RouterFunctions.route()
                .GET(this.agentCardEndpoint, this::handleAgentCard)
                .POST(this.messageEndpoint, this::handleMessage)
                .build();
    }


    @Override
    public void setSessionFactory(ServerSession.Factory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public Mono<Void> closeGracefully() {
        return Flux.fromIterable(sessions.values()).doFirst(() -> {
                    this.isClosing = true;
                    log.debug("Initiating graceful shutdown with {} active sessions", sessions.size());
                })
                .flatMap(ServerSession::closeGracefully)
                .then()
                .doOnSuccess(v -> log.debug("Graceful shutdown completed"));
    }


    private ServerResponse handleAgentCard(ServerRequest request) {
        if (this.isClosing) {
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body("Server is shutting down");
        }

        try {
            return ServerResponse.ok().body(OM.writeValueAsString(agentCard));
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
            JsonRpcRequest req = OM.readValue(body, JsonRpcRequest.class);
            String sessionId = req.getId();

            // streaming request
            if (new SendTaskStreamingRequest().getMethod().equalsIgnoreCase(req.getMethod())
                    || new TaskResubscriptionRequest().getMethod().equalsIgnoreCase(req.getMethod())) {
                return ServerResponse.sse(sseBuilder -> {
                    sseBuilder.onComplete(() -> {
                        ServerSession s = sessions.remove(sessionId);
                        long restEventSize = s.getRestEventSize(sessionId);
                        log.debug("SSE connection completed for session: {}, restEventSize: {}", sessionId, restEventSize);
                    });
                    sseBuilder.onTimeout(() -> {
                        ServerSession s = sessions.remove(sessionId);
                        long restEventSize = s.getRestEventSize(sessionId);
                        log.debug("SSE connection timed out for session: {}, restEventSize: {}", sessionId, restEventSize);
                    });

                    WebMvcSeeSessionTransport sessionTransport = new WebMvcSeeSessionTransport(sessionId, sseBuilder);
                    ServerSession session = sessionFactory.create(sessionTransport);
                    this.sessions.put(sessionId, session);

                    // sse handle
                    session.handleRequest(req);
                });
            }

            // common request
            return ServerResponse.ok().body(handleRequest(req).block());
        } catch (JacksonException e) {
            log.error("json parse error: {}", e.getMessage(), e);
            return ServerResponse.badRequest().body(new JSONParseError());
        } catch (IllegalArgumentException | IOException e) {
            log.error("invalid request error: {}", e.getMessage(), e);
            return ServerResponse.badRequest().body(new InvalidRequestError());
        } catch (Exception e) {
            log.error("handle request error: {}", e.getMessage(), e);
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new InternalError());
        }
    }

    public <T> Mono<JsonRpcResponse> handleRequest(JsonRpcRequest<T> request) {
        JsonRpcResponse response;
        if (new GetTaskRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            GetTaskRequest req = OM.convertValue(request, new TypeReference<GetTaskRequest>() {
            });
            response = taskManager.onGetTask(req);
        } else if (new SendTaskRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            SendTaskRequest req = OM.convertValue(request, new TypeReference<SendTaskRequest>() {
            });
            response = taskManager.onSendTask(req);
        } else if (new CancelTaskRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            CancelTaskRequest req = OM.convertValue(request, new TypeReference<CancelTaskRequest>() {
            });
            response = taskManager.onCancelTask(req);
        } else {
            throw new RuntimeException("unknown method");
        }
        return Mono.just(response);
    }

    /**
     * @author PheonixHkbxoic
     * @date 2025/4/11 18:52
     * @desc
     */
    @Slf4j
    public static class WebMvcSeeSessionTransport implements ServerTransport {

        private final String sessionId;
        private final ServerResponse.SseBuilder sseBuilder;

        /**
         * Creates a new session transport with the specified ID and SSE builder.
         *
         * @param sessionId  The unique identifier for this session
         * @param sseBuilder The SSE builder for sending server events to the client
         */
        WebMvcSeeSessionTransport(String sessionId, ServerResponse.SseBuilder sseBuilder) {
            this.sessionId = sessionId;
            this.sseBuilder = sseBuilder;
            log.debug("Session transport {} initialized with SSE builder", sessionId);
        }

        /**
         * Sends a JSON-RPC message to the client through the SSE connection.
         *
         * @param message The JSON-RPC message to send
         * @return A Mono that completes when the message has been sent
         */
        @Override
        public Mono<Void> sendMessage(JsonRpcMessage message) {
            return Mono.fromRunnable(() -> {
                try {
                    String jsonText = OM.writeValueAsString(message);
                    log.debug("sendMessage: {}", jsonText);
                    sseBuilder.id(sessionId).event("message").data(jsonText);
                } catch (Exception e) {
                    log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
                    sseBuilder.error(e);
                }
            });
        }


        /**
         * Converts data from one type to another using the configured ObjectMapper.
         *
         * @param data    The source data object to convert
         * @param typeRef The target type reference
         * @param <T>     The target type
         * @return The converted object of type T
         */
        @Override
        public <T> T unmarshalFrom(Object data, TypeReference<T> typeRef) {
            return OM.convertValue(data, typeRef);
        }

        /**
         * Initiates a graceful shutdown of the transport.
         *
         * @return A Mono that completes when the shutdown is complete
         */
        @Override
        public Mono<Void> closeGracefully() {
            return Mono.fromRunnable(() -> {
                log.debug("Closing session transport: {}", sessionId);
                try {
                    sseBuilder.complete();
                    log.debug("Successfully completed SSE builder for session {}", sessionId);
                } catch (Exception e) {
                    log.warn("Failed to complete SSE builder for session {}: {}", sessionId, e.getMessage());
                }
            });
        }

        /**
         * Closes the transport immediately.
         */
        @Override
        public void close() {
            try {

                sseBuilder.complete();
                log.debug("Successfully completed SSE builder for session {}", sessionId);
            } catch (Exception e) {
                log.warn("Failed to complete SSE builder for session {}: {}", sessionId, e.getMessage());
            }
        }

        public String getSessionId() {
            return sessionId;
        }
    }

}


