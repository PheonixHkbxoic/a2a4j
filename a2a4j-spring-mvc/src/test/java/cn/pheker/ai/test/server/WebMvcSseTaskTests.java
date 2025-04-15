/*
 * Copyright 2024 - 2024 the original author or authors.
 */
package cn.pheker.ai.test.server;

import cn.pheker.ai.client.A2AClient;
import cn.pheker.ai.client.AgentCardResolver;
import cn.pheker.ai.core.TaskManager;
import cn.pheker.ai.server.A2AServer;
import cn.pheker.ai.server.WebMvcSseServerTransportProvider;
import cn.pheker.ai.spec.entity.*;
import cn.pheker.ai.spec.error.JsonRpcError;
import cn.pheker.ai.spec.error.TaskNotFoundError;
import cn.pheker.ai.spec.message.GetTaskResponse;
import cn.pheker.ai.spec.message.SendTaskResponse;
import cn.pheker.ai.util.Util;
import cn.pheker.ai.util.Uuid;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class WebMvcSseTaskTests {

    private static final String baseUrl = "http://localhost:8080/";
    private static final int PORT = 8080;


    private static AgentCard agentCard;
    private static WebMvcSseServerTransportProvider serverTransportProvider;


    @Configuration
    @EnableWebMvc
    static class TestConfig {

        @Bean
        public AgentCard agentCard() {
            AgentCapabilities capabilities = new AgentCapabilities();
            AgentSkill skill = AgentSkill.builder().id("convert_currency").name("Currency Exchange Rates Tool").description("Helps with exchange values between various currencies").tags(Arrays.asList("currency conversion", "currency exchange")).examples(Collections.singletonList("What is exchange rate between USD and GBP?")).inputModes(Collections.singletonList("text")).outputModes(Collections.singletonList("text")).build();
            AgentCard agentCard = new AgentCard();
            agentCard.setName("Currency Agent");
            agentCard.setDescription("current exchange");
            agentCard.setUrl(baseUrl);
            agentCard.setVersion("1.0.0");
            agentCard.setCapabilities(capabilities);
            agentCard.setSkills(Collections.singletonList(skill));
            return agentCard;
        }

        @Bean
        public EchoAgent echoAgent() {
            return new EchoAgent();
        }

        @Bean
        public TaskManager taskManager() {
            return new EchoTaskManager(echoAgent());
        }

        @Bean
        public WebMvcSseServerTransportProvider webMvcSseServerTransportProvider() {
            return new WebMvcSseServerTransportProvider(agentCard(), taskManager());
        }

        @Bean
        public RouterFunction<ServerResponse> routerFunction(WebMvcSseServerTransportProvider transportProvider) {
            return transportProvider.getRouterFunction();
        }

    }


    private static Tomcat tomcat;
    private static AnnotationConfigWebApplicationContext appContext;
    private static A2AServer server;
    private static A2AClient client;

    @BeforeAll
    public static void before() {

        // Set up Tomcat first
        tomcat = new Tomcat();
        tomcat.setPort(PORT);

        // Set Tomcat base directory to java.io.tmpdir to avoid permission issues
        String baseDir = System.getProperty("java.io.tmpdir");
        tomcat.setBaseDir(baseDir);

        // Use the same directory for document base
        Context context = tomcat.addContext("", baseDir);

        // Create and configure Spring WebMvc context
        appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(TestConfig.class);
        appContext.setServletContext(context.getServletContext());
        appContext.refresh();

        agentCard = appContext.getBean(AgentCard.class);
        serverTransportProvider = appContext.getBean(WebMvcSseServerTransportProvider.class);

        // Create DispatcherServlet with our Spring context
        DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
        // dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);

        // Add servlet to Tomcat and get the wrapper
        Wrapper wrapper = Tomcat.addServlet(context, "dispatcherServlet", dispatcherServlet);
        wrapper.setLoadOnStartup(1);
        wrapper.setAsyncSupported(true);
        context.addServletMapping("/*", "dispatcherServlet");

        try {
            // Configure and start the connector with async support
            Connector connector = tomcat.getConnector();
            connector.setAsyncTimeout(3000); // 3 seconds timeout for async requests
            tomcat.start();
            assertThat(tomcat.getServer().getState() == LifecycleState.STARTED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Tomcat", e);
        }


        try {
            log.info("正在启动A2A server and client: {}", baseUrl);
            server = new A2AServer(agentCard, serverTransportProvider);

            AgentCardResolver resolver = new AgentCardResolver(baseUrl);
            AgentCard serverAgentCard = resolver.resolve();
            assertThat(serverAgentCard).isNotNull();
            log.info("agent card: {}", serverAgentCard);

            client = new A2AClient(agentCard);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void after() {
        if (serverTransportProvider != null) {
            serverTransportProvider.closeGracefully().block();
        }
        if (appContext != null) {
            appContext.close();
        }
        if (tomcat != null) {
            try {
                tomcat.stop();
                tomcat.destroy();
            } catch (LifecycleException e) {
                throw new RuntimeException("Failed to stop Tomcat", e);
            }
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
        GetTaskResponse taskResponse = client.getTask(params);
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
        SendTaskResponse taskResponse = client.sendTask(params);
        assertThat(taskResponse).isNotNull();
        assertThat(taskResponse.getError()).isNull();
        log.info("taskResponse: {}", Util.json(taskResponse));

        TaskQueryParams q = new TaskQueryParams();
        q.setId(params.getId());
        q.setHistoryLength(3);
        GetTaskResponse getTaskResponse = client.getTask(q);
        assertThat(getTaskResponse).isNotNull();
        log.info("getTaskResponse: {}", Util.json(getTaskResponse));
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
                .subscribe(r -> {
                    log.info("event data: {}", Util.json(r));
                });
        System.out.println("over " + params.getId());

    }


}
