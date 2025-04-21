package io.github.PheonixHkbxoic.a2a4j.core.core;

import io.github.PheonixHkbxoic.a2a4j.core.spec.message.JsonRpcRequest;
import io.github.PheonixHkbxoic.a2a4j.core.spec.message.JsonRpcResponse;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 */
public interface Session {

    <T> Mono<JsonRpcResponse> handleRequest(JsonRpcRequest<T> request);

    Mono<Void> closeGracefully();

    void close();

}