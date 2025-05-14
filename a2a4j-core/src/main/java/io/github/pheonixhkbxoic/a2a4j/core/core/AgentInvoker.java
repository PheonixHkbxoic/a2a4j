package io.github.pheonixhkbxoic.a2a4j.core.core;

import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Artifact;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Part;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.TaskSendParams;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.TextPart;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskRequest;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskStreamingRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/30 22:43
 * @desc agent invoker, used by TaskManager eg. DefaultTaskManager
 */
public interface AgentInvoker {

    /**
     * agent支持的输出模式
     *
     * @return 为空表示输出模式不限制
     */
    default List<String> acceptOutputModes() {
        return List.of();
    }

    /**
     * 设置 默认返回任务快照历史数量
     *
     * @return 默认返回任务快照历史数量
     */
    default int returnTaskHistoryNum() {
        return 5;
    }

    /**
     * invoke agent: agent will handle request and return Artifact list
     *
     * @param request SendTaskRequest
     * @return Artifact list
     */
    Mono<List<Artifact>> invoke(SendTaskRequest request);

    /**
     * invoke agent: agent will handle request and return StreamData one by one
     *
     * @param request SendTaskStreamingRequest
     * @return flux StreamData
     */
    Flux<StreamData> invokeStream(SendTaskStreamingRequest request);

    default String extractUserQuery(TaskSendParams ps) {
        List<Part> parts = ps.getMessage().getParts();
        return parts.stream()
                .filter(p -> p instanceof TextPart)
                .map(p -> ((TextPart) p).getText())
                .collect(Collectors.joining("\n"));
    }
}
