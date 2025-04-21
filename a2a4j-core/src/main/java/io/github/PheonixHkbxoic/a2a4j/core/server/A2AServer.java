package io.github.PheonixHkbxoic.a2a4j.core.server;

import io.github.PheonixHkbxoic.a2a4j.core.core.ServerAdapter;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.AgentCard;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 */
public class A2AServer {
    private AgentCard agentCard;
    private ServerAdapter serverAdapter;

    public A2AServer(AgentCard agentCard, ServerAdapter serverAdapter) {
        this.agentCard = agentCard;
        this.serverAdapter = serverAdapter;
    }

    public void close() {
        this.serverAdapter.close();
    }

    public Mono<Void> closeGracefully() {
        return this.serverAdapter.closeGracefully();
    }

}
