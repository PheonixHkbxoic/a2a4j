package io.github.pheonixhkbxoic.a2a4j.servlet.test;

import io.github.pheonixhkbxoic.a2a4j.core.client.A2AClient;
import io.github.pheonixhkbxoic.a2a4j.core.client.AgentCardResolver;
import io.github.pheonixhkbxoic.a2a4j.core.core.*;
import io.github.pheonixhkbxoic.a2a4j.core.server.A2AServer;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.*;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.JsonRpcError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.TaskNotFoundError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.GetTaskResponse;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.SendTaskResponse;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import io.github.pheonixhkbxoic.a2a4j.core.util.Uuid;
import io.github.pheonixhkbxoic.a2a4j.servlet.HttpServletSseServerAdapter;
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

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/23 19:30
 * @desc
 */
@Slf4j
public class HttpServletSseServerAdapterTests {
    private static final int PORT = 8901;
    private static final String baseUrl = "http://127.0.0.1:" + PORT;


    @Configuration
    static class TestConfig {

        @Bean
        public AgentCard agentCard() {
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

        @Bean
        public InMemoryTaskStore inMemoryTaskStore() {
            return new InMemoryTaskStore();
        }

        @Bean
        public EchoAgent echoAgent() {
            return new EchoAgent();
        }

        @Bean
        public AgentInvoker agentInvoker() {
            return new EchoAgentInvoker(echoAgent());
        }

        @Bean
        public TaskManager taskManager() {
            return new DefaultTaskManager(inMemoryTaskStore(), pushNotificationSenderAuth(), agentInvoker());
        }

        @Bean
        public PushNotificationSenderAuth pushNotificationSenderAuth() {
            return new PushNotificationSenderAuth();
        }

        @Bean
        public HttpServletSseServerAdapter httpServletSseServerAdapter() {
            return new HttpServletSseServerAdapter(agentCard(), taskManager(), null, pushNotificationSenderAuth());
        }

    }


    private static Tomcat tomcat;
    private static AnnotationConfigWebApplicationContext appContext;
    private static HttpServletSseServerAdapter httpServletSseServerAdapter;
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

        AgentCard agentCard = appContext.getBean(AgentCard.class);
        httpServletSseServerAdapter = appContext.getBean(HttpServletSseServerAdapter.class);

        // Create DispatcherServlet with our Spring context
//        DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
        // dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);

        // Add servlet to Tomcat and get the wrapper
        Wrapper wrapper = Tomcat.addServlet(context, "dispatcherServlet", httpServletSseServerAdapter);
        wrapper.setLoadOnStartup(1);
        wrapper.setAsyncSupported(true);
        context.addServletMappingDecoded("/*", "dispatcherServlet");

        try {
            // Configure and start the connector with async support
            Connector connector = tomcat.getConnector();
            connector.setAsyncTimeout(3000); // 3 seconds timeout for async requests
            tomcat.start();
            assertThat(tomcat.getServer().getState() == LifecycleState.STARTED).isTrue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Tomcat", e);
        }


        try {
            log.info("正在启动A2A server and client: {}", baseUrl);
            server = new A2AServer(agentCard, httpServletSseServerAdapter);
            server.start();

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
        if (httpServletSseServerAdapter != null) {
            httpServletSseServerAdapter.closeGracefully().block();
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
        GetTaskResponse taskResponse = client.getTask(params).block();
        assert taskResponse != null;
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
                .subscribe(r -> log.info("response {}", Util.toJson(r)));
        System.out.println("over " + params.getId());
    }

}
