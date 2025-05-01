package io.github.pheonixhkbxoic.a2a4j.core.server;

import io.github.pheonixhkbxoic.a2a4j.core.core.ServerAdapter;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCard;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 */
@Slf4j
@Getter
public class A2AServer {
    private AgentCard agentCard;
    private ServerAdapter serverAdapter;
    private String state = "closed";

    public A2AServer(AgentCard agentCard, ServerAdapter serverAdapter) {
        this.agentCard = agentCard;
        this.serverAdapter = serverAdapter;
    }

    public A2AServer close() {
        this.serverAdapter.close();
        log.info("A2AServer closed");
        this.state = "closed";
        return this;
    }

    public Mono<Void> closeGracefully() {
        return this.serverAdapter.closeGracefully();
    }

    public A2AServer start() {
        this.serverAdapter.start();
        log.info("A2AServer started");
        this.state = "running";
        return this;
    }

}
