package cn.pheker.ai.server;

import cn.pheker.ai.core.ServerSession;
import cn.pheker.ai.core.ServerTransportProvider;
import cn.pheker.ai.spec.entity.AgentCard;
import reactor.core.publisher.Mono;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:10
 * @desc
 */
public class A2AServer {
    private AgentCard agentCard;
    private ServerTransportProvider transportProvider;

    public A2AServer(AgentCard agentCard, ServerTransportProvider transportProvider) {
        this.agentCard = agentCard;
        this.transportProvider = transportProvider;
        this.transportProvider.setSessionFactory(sessionTransport -> new ServerSession(transportProvider.getTaskManager(), sessionTransport));
    }

    public void close() {
        this.closeGracefully().subscribe();
    }

    public Mono<Void> closeGracefully() {
        return this.transportProvider.closeGracefully();
    }

}
