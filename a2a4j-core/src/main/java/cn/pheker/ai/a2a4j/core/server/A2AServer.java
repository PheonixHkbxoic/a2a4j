package cn.pheker.ai.a2a4j.core.server;

import cn.pheker.ai.a2a4j.core.core.ServerAdapter;
import cn.pheker.ai.a2a4j.core.spec.entity.AgentCard;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:10
 * @desc
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
