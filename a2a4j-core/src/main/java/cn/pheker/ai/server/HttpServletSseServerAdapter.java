package cn.pheker.ai.server;

import cn.pheker.ai.core.ServerAdapter;
import cn.pheker.ai.core.ServerSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@WebServlet(asyncSupported = true)
public class HttpServletSseServerAdapter extends HttpServlet implements ServerAdapter {


    public static final String UTF_8 = "UTF-8";
    public static final String APPLICATION_JSON = "application/json";
    public static final String DEFAULT_SSE_ENDPOINT = "/sse";
    public static final String MESSAGE_EVENT_TYPE = "message";
    public static final String ENDPOINT_EVENT_TYPE = "endpoint";
    public static final String DEFAULT_BASE_URL = "";

    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String messageEndpoint;
    private final String sseEndpoint;
    private final ConcurrentMap<String, ServerSession> sessions = new ConcurrentHashMap<>();

    private final AtomicBoolean isClosing = new AtomicBoolean(false);
    private ServerSession.Factory sessionFactory;

    public HttpServletSseServerAdapter(ObjectMapper objectMapper, String messageEndpoint,
                                       String sseEndpoint) {
        this(objectMapper, DEFAULT_BASE_URL, messageEndpoint, sseEndpoint);
    }

    public HttpServletSseServerAdapter(ObjectMapper objectMapper, String baseUrl, String messageEndpoint,
                                       String sseEndpoint) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.messageEndpoint = messageEndpoint;
        this.sseEndpoint = sseEndpoint;
    }

    public HttpServletSseServerAdapter(ObjectMapper objectMapper, String messageEndpoint) {
        this(objectMapper, messageEndpoint, DEFAULT_SSE_ENDPOINT);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (!requestURI.endsWith(sseEndpoint)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (isClosing.get()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is shutting down");
            return;
        }

        response.setContentType("text/event-stream");
        response.setCharacterEncoding(UTF_8);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String sessionId = UUID.randomUUID().toString();
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0);

        PrintWriter writer = response.getWriter();

        // Create a new session transport
        HttpServletSessionTransport sessionTransport = new HttpServletSessionTransport(sessions, sessionId, asyncContext,
                writer, objectMapper);

        // Create a new session using the session factory
        ServerSession session = sessionFactory.create(sessionTransport);
        this.sessions.put(sessionId, session);

        // Send initial endpoint event
        this.sendEvent(writer, ENDPOINT_EVENT_TYPE, this.baseUrl + this.messageEndpoint + "?sessionId=" + sessionId);
    }

    /**
     * Handles POST requests for client messages.
     * <p>
     * This method processes incoming messages from clients, routes them through the
     * session handler, and sends back the appropriate response. It handles error cases
     * and formats error responses according to the  specification.
     *
     * @param request  The HTTP servlet request
     * @param response The HTTP servlet response
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException      If an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (isClosing.get()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is shutting down");
            return;
        }

        String requestURI = request.getRequestURI();
        if (!requestURI.endsWith(messageEndpoint)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Get the session ID from the request parameter
        String sessionId = request.getParameter("sessionId");
        if (sessionId == null) {
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(UTF_8);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String jsonError = objectMapper.writeValueAsString(new Error("Session ID missing in message endpoint"));
            PrintWriter writer = response.getWriter();
            writer.write(jsonError);
            writer.flush();
            return;
        }

        // Get the session from the sessions map
        ServerSession session = sessions.get(sessionId);
        if (session == null) {
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(UTF_8);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String jsonError = objectMapper.writeValueAsString(new Error("Session not found: " + sessionId));
            PrintWriter writer = response.getWriter();
            writer.write(jsonError);
            writer.flush();
            return;
        }

//        try {
//            BufferedReader reader = request.getReader();
//            StringBuilder body = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                body.append(line);
//            }
//
//            Schema.JSONRPCMessage message = Schema.deserializeJsonRpcMessage(objectMapper, body.toString());
//
//            // Process the message through the session's handle method
//            session.handle(message).block(); // Block for Servlet compatibility
//
//            response.setStatus(HttpServletResponse.SC_OK);
//        } catch (Exception e) {
//            log.error("Error processing message: {}", e.getMessage());
//            try {
//                Error Error = new Error(e.getMessage());
//                response.setContentType(APPLICATION_JSON);
//                response.setCharacterEncoding(UTF_8);
//                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                String jsonError = objectMapper.writeValueAsString(Error);
//                PrintWriter writer = response.getWriter();
//                writer.write(jsonError);
//                writer.flush();
//            } catch (IOException ex) {
//                log.error(FAILED_TO_SEND_ERROR_RESPONSE, ex.getMessage());
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing message");
//            }
//        }
    }

    @Override
    public Mono<Void> closeGracefully() {
        isClosing.set(true);
        log.debug("Initiating graceful shutdown with {} active sessions", sessions.size());

        return Flux.fromIterable(sessions.values()).flatMap(ServerSession::closeGracefully).then();
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


    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        private ObjectMapper objectMapper = new ObjectMapper();

        private String baseUrl = DEFAULT_BASE_URL;

        private String messageEndpoint;

        private String sseEndpoint = DEFAULT_SSE_ENDPOINT;


        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }


        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }


        public Builder messageEndpoint(String messageEndpoint) {
            this.messageEndpoint = messageEndpoint;
            return this;
        }

        public Builder sseEndpoint(String sseEndpoint) {
            this.sseEndpoint = sseEndpoint;
            return this;
        }

        public HttpServletSseServerAdapter build() {
            if (objectMapper == null) {
                throw new IllegalStateException("ObjectMapper must be set");
            }
            if (messageEndpoint == null) {
                throw new IllegalStateException("MessageEndpoint must be set");
            }
            return new HttpServletSseServerAdapter(objectMapper, baseUrl, messageEndpoint, sseEndpoint);
        }

    }

}