package io.github.PheonixHkbxoic.a2a4j.servlet;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.PheonixHkbxoic.a2a4j.core.core.PushNotificationSenderAuth;
import io.github.PheonixHkbxoic.a2a4j.core.core.ServerAdapter;
import io.github.PheonixHkbxoic.a2a4j.core.core.TaskManager;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.InternalError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.InvalidRequestError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.JSONParseError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.MethodNotFoundError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.message.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/23 19:24
 * @desc
 */
@Slf4j
@WebServlet(asyncSupported = true)
public class HttpServletSseServerAdapter extends HttpServlet implements ServerAdapter {
    private final ObjectMapper om;
    private String messageEndpoint = "/";
    private String agentCardEndpoint = "/.well-known/agent.json";
    private String jwksEndpoint = "/.well-known/jwks.json";
    private final AgentCard agentCard;
    private final TaskManager taskManager;
    private final Validator validator;
    private final PushNotificationSenderAuth auth;

    private volatile boolean isClosing = false;

    public HttpServletSseServerAdapter(AgentCard agentCard, TaskManager taskManager, Validator validator, PushNotificationSenderAuth auth) {
        this(new ObjectMapper(), null, null, agentCard, taskManager, validator, null, auth);
    }

    public HttpServletSseServerAdapter(ObjectMapper objectMapper, String messageEndpoint,
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
    }

    public static final String UTF_8 = "UTF-8";
    public static final String APPLICATION_JSON = "application/json";


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (isClosing) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is shutting down");
            return;
        }

        String requestUri = request.getRequestURI();
        if (requestUri.endsWith(agentCardEndpoint)) {
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(UTF_8);
            response.setStatus(HttpServletResponse.SC_OK);
            String json = om.writeValueAsString(agentCard);
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
        } else if (requestUri.endsWith(jwksEndpoint)) {
            Map<String, Object> data = Collections.singletonMap("keys", Collections.singletonList(auth.getPublicKey().toJSONObject()));
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(UTF_8);
            response.setStatus(HttpServletResponse.SC_OK);
            String json = om.writeValueAsString(data);
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (isClosing) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is shutting down");
            return;
        }

        String requestUri = request.getRequestURI();
        if (!requestUri.endsWith(messageEndpoint)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            JsonRpcRequest<Object> req = om.readValue(body.toString(), JsonRpcRequest.class);
            if (validator != null) {
                validator.validate(req);
            }

            // streaming request
            if (new SendTaskStreamingRequest().getMethod().equalsIgnoreCase(req.getMethod())
                    || new TaskResubscriptionRequest().getMethod().equalsIgnoreCase(req.getMethod())) {
                this.handleRequestSse(request, response, req);
                return;
            }

            // common request
            this.handleRequest(response, req);
        } catch (JacksonException e) {
            log.error("json parse error: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, om.writeValueAsString(new JSONParseError()));
        } catch (IllegalArgumentException | IllegalStateException | ValidationException e) {
            log.error("invalid request error: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, om.writeValueAsString(new InvalidRequestError()));
        } catch (Exception e) {
            log.error("handle request error: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, om.writeValueAsString(new InternalError()));
        }
    }

    public <T> void handleRequestSse(HttpServletRequest request, HttpServletResponse response, JsonRpcRequest<T> rpcRequest) throws IOException {
        Mono<? extends JsonRpcResponse<?>> monoResponse;
        String taskId;
        if (new SendTaskStreamingRequest().getMethod().equalsIgnoreCase(rpcRequest.getMethod())) {
            SendTaskStreamingRequest req = om.convertValue(rpcRequest, new TypeReference<SendTaskStreamingRequest>() {
            });
            monoResponse = taskManager.onSendTaskSubscribe(req);
            taskId = req.getParams().getId();
        } else if (new TaskResubscriptionRequest().getMethod().equalsIgnoreCase(rpcRequest.getMethod())) {
            TaskResubscriptionRequest req = om.convertValue(rpcRequest, new TypeReference<TaskResubscriptionRequest>() {
            });
            monoResponse = taskManager.onResubscribeTask(req);
            taskId = req.getParams().getId();
        } else {
            monoResponse = Mono.just(new JsonRpcResponse<>(rpcRequest.getId(), new MethodNotFoundError()));
            taskId = "";
        }

        // exception return rpcResponse
        JsonRpcResponse<?> rpcResponse = monoResponse.block();
        if (rpcResponse != null) {
            this.sendMessage(response, rpcResponse);
            return;
        }

        // sse response
        response.setContentType("text/event-stream");
        response.setCharacterEncoding(UTF_8);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");

        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);

        PrintWriter writer = response.getWriter();

        // sse handle
        taskManager.dequeueEvent(taskId)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> writer.close())
                .doOnComplete(writer::close)
                .subscribe(updateEvent -> {
                    log.debug("dequeueEvent taskId: {}, updateEvent: {}", taskId, updateEvent);
                    try {
                        String message = om.writeValueAsString(new SendTaskStreamingResponse(rpcRequest.getId(), updateEvent));
                        this.sendEvent(writer, EVENT_MESSAGE, message);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        log.info(e.getMessage());
                        writer.close();
                    }
                });
    }

    public <T> void handleRequest(HttpServletResponse httpServletResponse, JsonRpcRequest<T> request) throws IOException {
        Mono<? extends JsonRpcResponse<?>> response;
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
            response = Mono.just(new JsonRpcResponse<>(request.getId(), new MethodNotFoundError()));
        }

        this.sendMessage(httpServletResponse, response.block());
    }

    public void sendMessage(HttpServletResponse httpServletResponse, JsonRpcResponse<?> jsonRpcResponse) throws IOException {
        httpServletResponse.setContentType(APPLICATION_JSON);
        httpServletResponse.setCharacterEncoding(UTF_8);
        httpServletResponse.setStatus(HttpStatus.SC_OK);
        String jsonError = om.writeValueAsString(jsonRpcResponse);
        PrintWriter writer = httpServletResponse.getWriter();
        writer.write(jsonError);
        writer.flush();
    }

    @Override
    public Mono<Void> closeGracefully() {
        isClosing = true;
        return this.taskManager.closeGracefully();
    }

    /**
     * Sends an SSE event to a client.
     *
     * @param writer    The writer to send the event through
     * @param eventType The type of event (message or endpoint)
     * @param data      The event data
     * @throws IOException If an error occurs while writing the event
     */
    private void sendEvent(PrintWriter writer, String eventType, String data) throws IOException {
        writer.write("event: " + eventType + "\n");
        writer.write("data: " + data + "\n\n");
        writer.flush();

        if (writer.checkError()) {
            throw new IOException("Client disconnected");
        }
    }

    @Override
    public void destroy() {
        closeGracefully().block();
        super.destroy();
    }


}