package io.github.PheonixHkbxoic.a2a4j.core.core;

import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 */
public interface ServerAdapter {
    default void close() {
        this.closeGracefully().subscribe();
    }

    Mono<Void> closeGracefully();

}