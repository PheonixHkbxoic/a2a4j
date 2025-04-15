package cn.pheker.ai.core;

import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 16:26
 * @desc
 */
public interface ServerTransportProvider {
    TaskManager getTaskManager();

    void setSessionFactory(ServerSession.Factory sessionFactory);

    default void close() {
        this.closeGracefully().subscribe();
    }

    Mono<Void> closeGracefully();

}