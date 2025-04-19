package cn.pheker.ai.a2a4j.core.core;

import cn.pheker.ai.a2a4j.core.spec.message.JsonRpcRequest;
import cn.pheker.ai.a2a4j.core.spec.message.JsonRpcResponse;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 16:28
 * @desc
 */
public interface Session {

    <T> Mono<JsonRpcResponse> handleRequest(JsonRpcRequest<T> request);

    Mono<Void> closeGracefully();

    void close();

}