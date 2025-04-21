package io.github.PheonixHkbxoic.a2a4j.core.core;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.JSONParseError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.error.MethodNotFoundError;
import io.github.PheonixHkbxoic.a2a4j.core.spec.message.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import javax.xml.validation.Validator;

/**
 * @author PheonixHkbxoic
 */
@Data
@Slf4j
public class ServerSession implements Session {
    private final TaskManager taskManager;
    private final ServerTransport transport;

    public ServerSession(TaskManager taskManager, ServerTransport transport, Validator validator) {
        this.taskManager = taskManager;
        this.transport = transport;

    }


    @Override
    public <T> Mono<JsonRpcResponse> handleRequest(JsonRpcRequest<T> request) {
        Mono<JsonRpcResponse> rpcResponseMono;
        String taskId;
        try {
            if (new SendTaskStreamingRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
                SendTaskStreamingRequest req = transport.unmarshalFrom(request, new TypeReference<SendTaskStreamingRequest>() {
                });
                rpcResponseMono = taskManager.onSendTaskSubscribe(req);
                taskId = req.getParams().getId();
            } else if (new TaskResubscriptionRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
                TaskResubscriptionRequest req = transport.unmarshalFrom(request, new TypeReference<TaskResubscriptionRequest>() {
                });
                rpcResponseMono = taskManager.onResubscribeTask(req);
                taskId = req.getParams().getId();
            } else {
                rpcResponseMono = Mono.just(new JsonRpcResponse(request.getId(), new MethodNotFoundError()));
                taskId = "";
            }

            // exception return rpcResponse
            JsonRpcResponse rpcResponse = rpcResponseMono.block();
            if (rpcResponse != null) {
                return Mono.just(rpcResponse);
            }

            // consumer
            taskManager.dequeueEvent(taskId)
                    .subscribe(updateEvent -> {
                        log.debug("dequeueEvent taskId: {}, updateEvent: {}", taskId, updateEvent);
                        transport.sendMessage(new SendTaskStreamingResponse(request.getId(), updateEvent)).block();
                    });
        } catch (IllegalArgumentException e) {
            return Mono.just(new JsonRpcResponse<>(request.getId(), new JSONParseError()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Mono.empty();
    }


    @Override
    public Mono<Void> closeGracefully() {
        return transport.closeGracefully();
    }

    @Override
    public void close() {
        this.closeGracefully().block();
    }


    @FunctionalInterface
    public interface Factory {
        ServerSession create(ServerTransport sessionTransport);
    }

}
