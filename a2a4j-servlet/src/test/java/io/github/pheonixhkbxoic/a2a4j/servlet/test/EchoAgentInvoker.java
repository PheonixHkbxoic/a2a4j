package io.github.pheonixhkbxoic.a2a4j.servlet.test;

import io.github.pheonixhkbxoic.a2a4j.core.core.AgentInvoker;
import io.github.pheonixhkbxoic.a2a4j.core.core.StreamData;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskRequest;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskStreamingRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author PheonixHkbxoic
 * @date 2025/5/1 01:35
 * @desc
 */
public class EchoAgentInvoker implements AgentInvoker {
    private EchoAgent agent;

    public EchoAgentInvoker(EchoAgent agent) {
        this.agent = agent;
    }

    @Override
    public Mono<List<Artifact>> invoke(SendTaskRequest request) {
        String userQuery = this.extractUserQuery(request.getParams());
        return agent.chat(userQuery)
                .map(text -> {
                    Artifact artifact = Artifact.builder().name("answer").parts(List.of(new TextPart(text))).build();
                    return List.of(artifact);
                });
    }

    @Override
    public Flux<StreamData> invokeStream(SendTaskStreamingRequest request) {
        String userQuery = this.extractUserQuery(request.getParams());
        return agent.chatStream(userQuery)
                .map(text -> {
                    Message message = Message.builder().role(Role.AGENT).parts(List.of(new TextPart(text))).build();
                    return StreamData.builder().state(TaskState.WORKING).message(message).endStream(false).build();
                })
                .concatWithValues(StreamData.builder()
                        .state(TaskState.COMPLETED)
                        .message(Message.builder().role(Role.AGENT).parts(List.of(new TextPart(""))).build())
                        .endStream(true)
                        .build());
    }
}
