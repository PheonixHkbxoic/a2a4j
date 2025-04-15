package cn.pheker.ai.core;

import cn.pheker.ai.spec.ValueError;
import cn.pheker.ai.spec.error.MethodNotFoundError;
import cn.pheker.ai.spec.message.JsonRpcRequest;
import cn.pheker.ai.spec.message.SendTaskStreamingRequest;
import cn.pheker.ai.spec.message.SendTaskStreamingResponse;
import cn.pheker.ai.spec.message.TaskResubscriptionRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 16:29
 * @desc
 */
@Data
@Slf4j
public class ServerSession implements Session {
    private final TaskManager taskManager;
    private final ServerTransport transport;

    public ServerSession(TaskManager taskManager, ServerTransport transport) {
        this.taskManager = taskManager;
        this.transport = transport;

    }


    @Override
    public <T> Mono<Void> handleRequest(JsonRpcRequest<T> request) {
        String taskId;
        if (new SendTaskStreamingRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            SendTaskStreamingRequest req = transport.unmarshalFrom(request, new TypeReference<SendTaskStreamingRequest>() {
            });
            taskManager.onSendTaskSubscribe(req);
            taskId = req.getParams().getId();
        } else if (new TaskResubscriptionRequest().getMethod().equalsIgnoreCase(request.getMethod())) {
            TaskResubscriptionRequest req = transport.unmarshalFrom(request, new TypeReference<TaskResubscriptionRequest>() {
            });
            taskManager.onResubscribeTask(req).block();
            taskId = req.getParams().getId();
        } else {
            return Mono.error(new ValueError(new MethodNotFoundError().getMessage()));
        }

        // consumer
        return taskManager.dequeueEvent(taskId, updateEvent -> {
            log.debug("dequeueEvent taskId: {}, updateEvent: {}", taskId, updateEvent);
            transport.sendMessage(new SendTaskStreamingResponse(request.getId(), updateEvent)).block();
        });
    }


    @Override
    public Mono<Void> closeGracefully() {
        return transport.closeGracefully();
    }

    @Override
    public long getRestEventSize(String sessionId) {
        return taskManager.getRestEventSize(sessionId);
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
