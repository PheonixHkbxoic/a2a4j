/*
 * Copyright 2024 - 2024 the original author or authors.
 */
package io.github.pheonixhkbxoic.a2a4j.mvc.test;

import io.github.pheonixhkbxoic.a2a4j.core.client.AgentCardResolver;
import io.github.pheonixhkbxoic.a2a4j.core.core.*;
import io.github.pheonixhkbxoic.a2a4j.core.server.A2AServer;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCapabilities;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCard;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentSkill;
import io.github.pheonixhkbxoic.a2a4j.mvc.WebMvcSseServerAdapter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
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
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class WebMvcSseIntegrationTests {

    private static final String baseUrl = "http://localhost:8080/";
    private static final int PORT = 8080;


    private AgentCard agentCard;
    private WebMvcSseServerAdapter serverTransportProvider;


    @EnableWebMvc// autoconfigure HttpMessageConverter
    @Configuration
    static class TestConfig {

        @Bean
        public AgentCard agentCard() {
            AgentCapabilities capabilities = new AgentCapabilities();
            AgentSkill skill = AgentSkill.builder()
                    .id("convert_currency").name("Currency Exchange Rates Tool")
                    .description("Helps with exchange values between various currencies")
                    .tags(Arrays.asList("currency conversion", "currency exchange"))
                    .examples(Collections.singletonList("What is exchange rate between USD and GBP?"))
                    .inputModes(Collections.singletonList("text"))
                    .outputModes(Collections.singletonList("text"))
                    .build();
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

        @Primary
        @Bean
        public LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        public WebMvcSseServerAdapter webMvcSseServerTransportProvider(Validator validator) {
            return new WebMvcSseServerAdapter(agentCard(), taskManager(), validator, pushNotificationSenderAuth());
        }

        @Bean
        public RouterFunction<ServerResponse> routerFunction(WebMvcSseServerAdapter transportProvider) {
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
        serverTransportProvider = appContext.getBean(WebMvcSseServerAdapter.class);

        // Create DispatcherServlet with our Spring context
        DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
        // dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);

        // Add servlet to Tomcat and get the wrapper
        Wrapper wrapper = Tomcat.addServlet(context, "dispatcherServlet", dispatcherServlet);
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
        server.start();
        AgentCardResolver resolver = new AgentCardResolver(baseUrl);
        AgentCard serverAgentCard = resolver.resolve();
        assertThat(serverAgentCard).isNotNull();
        log.info("serverAgentCard: {}", serverAgentCard);
        server.close();
    }


    @Data
    static class User {
        @NotBlank
        private String id;

        @Valid
        @NotNull
        private User2 user2;

        public User(String id, User2 user2) {
            this.id = id;
            this.user2 = user2;
        }

    }

    static class User2 {
        @NotBlank
        private String id;


        public User2(String id) {
            this.id = id;
        }
    }

    @Test
    public void testValidator() {
        Validator validator = appContext.getBean(Validator.class);
        Set<ConstraintViolation<User>> s1 = validator.validate(new User(null, null));
        assertThat(s1).isNotEmpty();
        Set<ConstraintViolation<User>> s2 = validator.validate(new User("1", null));
        assertThat(s2).isNotEmpty();
        Set<ConstraintViolation<User>> s3 = validator.validate(new User("1", new User2(null)));
        assertThat(s3).isNotEmpty();
        Set<ConstraintViolation<User>> s4 = validator.validate(new User("1", new User2("11")));
        assertThat(s4).isEmpty();

    }


}
