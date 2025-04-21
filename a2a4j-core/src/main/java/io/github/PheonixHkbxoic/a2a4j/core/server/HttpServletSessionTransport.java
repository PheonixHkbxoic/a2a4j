package io.github.PheonixHkbxoic.a2a4j.core.server;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/21 23:56
 * @desc
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.PheonixHkbxoic.a2a4j.core.core.ServerSession;
import io.github.PheonixHkbxoic.a2a4j.core.core.ServerTransport;
import io.github.PheonixHkbxoic.a2a4j.core.spec.message.JsonRpcMessage;
import jakarta.servlet.AsyncContext;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class HttpServletSessionTransport implements ServerTransport {
    private final ConcurrentMap<String, ServerSession> sessions;
    private final String sessionId;
    private final AsyncContext asyncContext;
    private final PrintWriter writer;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new session transport with the specified ID and SSE writer.
     *
     * @param sessionId    The unique identifier for this session
     * @param asyncContext The async context for the session
     * @param writer       The writer for sending server events to the client
     */
    HttpServletSessionTransport(ConcurrentMap<String, ServerSession> sessions, String sessionId, AsyncContext asyncContext, PrintWriter writer, ObjectMapper objectMapper) {
        this.sessions = sessions;
        this.sessionId = sessionId;
        this.asyncContext = asyncContext;
        this.writer = writer;
        this.objectMapper = objectMapper;
        log.debug("Session transport {} initialized with SSE writer", sessionId);
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
                String jsonText = objectMapper.writeValueAsString(message);
//                writer.write("event: " + eventType + "\n");
//                writer.write("data: " + data + "\n\n");
                writer.write("data: " + jsonText + "\n\n");
                writer.flush();

                if (writer.checkError()) {
                    throw new IOException("Client disconnected");
                }
                log.debug("Message sent to session {}", sessionId);
            } catch (Exception e) {
                log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
                sessions.remove(sessionId);
                asyncContext.complete();
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
        return objectMapper.convertValue(data, typeRef);
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
                sessions.remove(sessionId);
                asyncContext.complete();
                log.debug("Successfully completed async context for session {}", sessionId);
            } catch (Exception e) {
                log.warn("Failed to complete async context for session {}: {}", sessionId, e.getMessage());
            }
        });
    }

    /**
     * Closes the transport immediately.
     */
    @Override
    public void close() {
        try {
            sessions.remove(sessionId);
            asyncContext.complete();
            log.debug("Successfully completed async context for session {}", sessionId);
        } catch (Exception e) {
            log.warn("Failed to complete async context for session {}: {}", sessionId, e.getMessage());
        }
    }

}
