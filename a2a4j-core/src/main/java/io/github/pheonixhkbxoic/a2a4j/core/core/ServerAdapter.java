package io.github.pheonixhkbxoic.a2a4j.core.core;

import io.github.pheonixhkbxoic.a2a4j.core.server.A2AServer;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 */
public interface ServerAdapter {
    default void close() {
        this.closeGracefully().subscribe();
    }

    Mono<Void> closeGracefully();

    String EVENT_MESSAGE = "message";

    void start();

    A2AServer getServer();
}