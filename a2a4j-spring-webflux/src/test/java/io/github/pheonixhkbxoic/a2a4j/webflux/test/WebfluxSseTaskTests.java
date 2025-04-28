/*
 * Copyright 2024 - 2024 the original author or authors.
 */
package io.github.pheonixhkbxoic.a2a4j.webflux.test;

import io.github.pheonixhkbxoic.a2a4j.core.client.A2AClient;
import io.github.pheonixhkbxoic.a2a4j.core.client.AgentCardResolver;
import io.github.pheonixhkbxoic.a2a4j.core.core.PushNotificationSenderAuth;
import io.github.pheonixhkbxoic.a2a4j.core.server.A2AServer;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.JsonRpcError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.TaskNotFoundError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.GetTaskResponse;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskResponse;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import io.github.pheonixhkbxoic.a2a4j.core.util.Uuid;
import io.github.pheonixhkbxoic.a2a4j.webflux.WebfluxSseServerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class WebfluxSseTaskTests {

    private static final int PORT = 8080;
    private static final String baseUrl = "http://localhost:" + PORT;


    public static AgentCard agentCard() {
        AgentCapabilities capabilities = new AgentCapabilities();
        AgentSkill skill = AgentSkill.builder().id("convert_currency").name("Currency Exchange Rates Tool").description("Helps with exchange values between various currencies").tags(Arrays.asList("currency conversion", "currency exchange")).examples(Collections.singletonList("What is exchange rate between USD and GBP?")).inputModes(Collections.singletonList("text")).outputModes(Collections.singletonList("text")).build();
        AgentCard agentCard = new AgentCard();
        agentCard.setName("Currency Agent");
        agentCard.setDescription("current exchange");
        agentCard.setUrl(baseUrl);
        agentCard.setVersion("1.0.1");
        agentCard.setCapabilities(capabilities);
        agentCard.setSkills(Collections.singletonList(skill));
        return agentCard;
    }


    private DisposableServer httpServer;
    private A2AServer server;
    private A2AClient client;

    @BeforeEach
    public void before() {
        EchoAgent echoAgent = new EchoAgent();
        AgentCard agentCard = agentCard();
        PushNotificationSenderAuth pushNotificationSenderAuth = new PushNotificationSenderAuth();
        EchoTaskManager taskManager = new EchoTaskManager(echoAgent, pushNotificationSenderAuth);
        WebfluxSseServerAdapter webFluxSseServerAdapter = new WebfluxSseServerAdapter(agentCard, taskManager, null, pushNotificationSenderAuth);
        HttpHandler httpHandler = RouterFunctions.toHttpHandler(webFluxSseServerAdapter.getRouterFunction());
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        this.httpServer = HttpServer.create().port(PORT).handle(adapter).bindNow();


        try {
            log.info("正在启动A2A server and client: {}", baseUrl);
            server = new A2AServer(agentCard, webFluxSseServerAdapter);

            AgentCardResolver resolver = new AgentCardResolver(baseUrl);
            AgentCard serverAgentCard = resolver.resolve();
            assertThat(serverAgentCard).isNotNull();
            log.info("agent card: {}", serverAgentCard);

            client = new A2AClient(serverAgentCard);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void after() {
        if (httpServer != null) {
            httpServer.disposeNow();
        }

        if (server != null) {
            server.close();
        }
        if (client != null) {
            client.close();
        }

        log.info("测试完成");
    }


    @Test
    public void testGetTaskResponse_TaskNotFoundError() {
        TaskQueryParams params = new TaskQueryParams();
        params.setId("1");
        params.setHistoryLength(3);
        GetTaskResponse taskResponse = client.getTask(params).block();
        JsonRpcError error = taskResponse.getError();
        assertThat(error).isNotNull().extracting("code").isEqualTo(new TaskNotFoundError().getCode());
    }

    @Test
    public void testSendTaskResponse() {
        TaskSendParams params = TaskSendParams.builder()
                .id(Uuid.uuid4hex())
                .sessionId(Uuid.uuid4hex())
                .historyLength(3)
                .message((Message.builder().role(Role.USER)).parts(Collections.singletonList(new TextPart("100块人民币能总汇多少美元"))).build())
                .build();
        SendTaskResponse taskResponse = client.sendTask(params).block();
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getError()).isNull();
        log.info("taskResponse: {}", Util.toJson(taskResponse));

        TaskQueryParams q = new TaskQueryParams();
        q.setId(params.getId());
        q.setHistoryLength(3);
        GetTaskResponse getTaskResponse = client.getTask(q).block();
        assertThat(getTaskResponse).isNotNull();
        log.info("getTaskResponse: {}", Util.toJson(getTaskResponse));
    }

    @Test
    public void testSendTaskSubscribeResponse() {
        TaskSendParams params = TaskSendParams.builder()
                .id(Uuid.uuid4hex())
                .sessionId(Uuid.uuid4hex())
                .historyLength(3)
                .message((Message.builder().role(Role.USER)).parts(Collections.singletonList(new TextPart("100块人民币能总汇多少美元"))).build())
                .build();
        client.sendTaskSubscribe(params)
                .doOnError(System.err::println)
                .subscribe(r -> log.info("testSendTaskSubscribeResponse response {}", Util.toJson(r)));
        System.out.println("over " + params.getId());
    }


    @Test
    public void testSendTaskSubscribeResponse2() {
        TaskSendParams params = TaskSendParams.builder()
                .id(Uuid.uuid4hex())
                .sessionId(Uuid.uuid4hex())
                .historyLength(3)
                .message((Message.builder().role(Role.USER)).parts(Collections.singletonList(new TextPart("100块人民币能总汇多少美元"))).build())
                .build();
        client.sendTaskSubscribe(params)
                .doOnError(System.err::println)
                .subscribe(r -> log.info("response1 {}", Util.toJson(r)));
        System.out.println("over1 " + params.getId());

        TaskIdParams ps = new TaskIdParams();
        ps.setId(params.getId());
        client.sendTaskResubscribe(ps)
                .doOnError(System.err::println)
                .subscribe(r -> log.info("response2 {}", Util.toJson(r)));
        System.out.println("over2 " + params.getId());
    }

    @Test
    public void testSendTaskResubscribe() {
        TaskIdParams params = new TaskIdParams();
        params.setId(Uuid.uuid4hex());
        client.sendTaskResubscribe(params)
                .doOnError(System.out::println)
                .subscribe(r -> log.info("testSendTaskResubscribe response {}", Util.toJson(r)));
        System.out.println("over " + params.getId());
    }


}
