/*
 * Copyright 2024 - 2024 the original author or authors.
 */
package cn.pheker.ai.test.server;

import cn.pheker.ai.client.AgentCardResolver;
import cn.pheker.ai.core.InMemoryTaskManager;
import cn.pheker.ai.core.TaskManager;
import cn.pheker.ai.server.A2AServer;
import cn.pheker.ai.server.WebMvcSseServerTransportProvider;
import cn.pheker.ai.spec.ValueError;
import cn.pheker.ai.spec.entity.AgentCapabilities;
import cn.pheker.ai.spec.entity.AgentCard;
import cn.pheker.ai.spec.entity.AgentSkill;
import cn.pheker.ai.spec.entity.Task;
import cn.pheker.ai.spec.message.SendTaskRequest;
import cn.pheker.ai.spec.message.SendTaskResponse;
import cn.pheker.ai.spec.message.SendTaskStreamingRequest;
import cn.pheker.ai.spec.message.TaskResubscriptionRequest;
import cn.pheker.ai.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class WebMvcSseIntegrationTests {

    private static final String baseUrl = "http://localhost:8080/";
    private static final int PORT = 8080;


    private AgentCard agentCard;
    private WebMvcSseServerTransportProvider serverTransportProvider;


    @Configuration
    @EnableWebMvc
    static class TestConfig {

        @Bean
        public AgentCard agentCard() {
            AgentCapabilities capabilities = new AgentCapabilities();
            AgentSkill skill = AgentSkill.builder().id("convert_currency").name("Currency Exchange Rates Tool").description("Helps with exchange values between various currencies").tags(Arrays.asList("currency conversion", "currency exchange")).examples(Arrays.asList("What is exchange rate between USD and GBP?")).inputModes(Arrays.asList("text")).outputModes(Arrays.asList("text")).build();
            AgentCard agentCard = new AgentCard();
            agentCard.setName("Currency Agent");
            agentCard.setDescription("current exchange");
            agentCard.setUrl(baseUrl);
            agentCard.setVersion("1.0.0");
            agentCard.setCapabilities(capabilities);
            agentCard.setSkills(Arrays.asList(skill));
            return agentCard;
        }

        @Bean
        public TaskManager taskManager() {
            return new InMemoryTaskManager() {
                @Override
                public SendTaskResponse onSendTask(SendTaskRequest request) {
                    log.info("sendTaskRequest: {}", request);
                    return new SendTaskResponse(Task.builder().build());
                }

                @Override
                public Mono<Void> onSendTaskSubscribe(SendTaskStreamingRequest request) {
                    return Mono.empty();
                }

                @Override
                public Mono<Void> onResubscribeTask(TaskResubscriptionRequest request) {
                    return Mono.error(new ValueError(Util.newNotImplementedError(request.getId()).getError().getMessage()));
                }
            };
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


    private Tomcat tomcat;
    private AnnotationConfigWebApplicationContext appContext;

    @BeforeEach
    public void before() {

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

//        this.clientBuilder = McpClient.sync(new HttpClientSseClientTransport("http://localhost:" + PORT));
    }

    @AfterEach
    public void after() {
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
    }


    @Test
    public void testInitialize() {
        A2AServer server = new A2AServer(agentCard, serverTransportProvider);
        AgentCardResolver resolver = new AgentCardResolver(baseUrl);
        AgentCard serverAgentCard = resolver.resolve();
        assertThat(serverAgentCard).isNotNull();
        log.info("serverAgentCard: {}", serverAgentCard);
        server.close();
    }

}
