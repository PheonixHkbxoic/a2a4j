package cn.pheker.ai.a2a4j.core.core;

import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 16:26
 * @desc
 */
public interface ServerAdapter {
    default void close() {
        this.closeGracefully().subscribe();
    }

    Mono<Void> closeGracefully();

}