package cn.pheker.ai.core;

import cn.pheker.ai.spec.message.JsonRpcMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 16:21
 * @desc
 */
public interface Transport {

    default void close() {
        this.closeGracefully().subscribe();
    }

    Mono<Void> closeGracefully();

    Mono<Void> sendMessage(JsonRpcMessage message);

    <T> T unmarshalFrom(Object data, TypeReference<T> typeRef);


}
