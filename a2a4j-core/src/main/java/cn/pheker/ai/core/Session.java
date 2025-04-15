package cn.pheker.ai.core;

import cn.pheker.ai.spec.message.JsonRpcRequest;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 16:28
 * @desc
 */
public interface Session {

    <T> Mono<Void> handleRequest(JsonRpcRequest<T> request);

    Mono<Void> closeGracefully();

    void close();

    long getRestEventSize(String sessionId);
}