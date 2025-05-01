[TOC]

# a2a4j

agent to agent scenarios with google a2a protocol

* Seamless Agent Collaboration: Introduces a standard protocol for autonomous, opaque agents built on different
  frameworks
  and by various vendors to communicate and collaborate effectively with each other and with users, addressing the
  current
  lack of agent interoperability.
* Simplifies Enterprise Agent Integration: Provides a straightforward way to integrate intelligent agents into existing
  enterprise applications, allowing businesses to leverage agent capabilities across their technology landscape.
* Supports Key Enterprise Requirements: Offers core functionalities essential for secure, enterprise-grade agent
  ecosystems, including capability discovery, user experience negotiation, task and state management, and secure
  collaboration.

## Open standards for connecting Agents

* MCP (Model Context Protocol) for tools and resources
    * Connect agents to tools, APIs, and resources with structured inputs/outputs.
    * Google ADK supports MCP tools. Enabling wide range of MCP servers to be used with agents.
* A2A (Agent2Agent Protocol) for agent-agent collaboration
    * Dynamic, multimodal communication between different agents without sharing memory, resources, and tools
    * Open standard driven by community.
    * Samples available using Google ADK, LangGraph, Crew.AI

To understand A2A design principles and external partners supporting
A2A, [public blog post](https://developers.googleblog.com/en/a2a-a-new-era-of-agent-interoperability/)

Interested to contribute and know more about the internals of A2A protocol ? [Github](https://github.com/google/A2A)

## 什么是 A2A 协议

A2A（Agent2Agent）协议 是由 Google Cloud 推出的一个开放协议，旨在促进不同 AI
代理之间的互操作性。其主要目标是允许这些代理在动态的、多代理的生态系统中进行有效的通信和协作，无论它们是由不同的供应商构建的还是使用不同的技术框架。

## A2A协议如何工作？

1. 能力说明书（Agent Card）  
   每个AI智能体需提供一份JSON格式的“说明书”，明确告知其他智能体：“我能做什么”（如分析数据、预订机票）、“需要什么输入格式”、“如何验证身份”。这相当于企业员工的名片，让协作方快速识别可用资源，其他智能体想合作就能很快找到它、理解它的能力，省去了大量沟通障碍。
2. 任务派发与追踪  
   当一个智能体想委托另一个智能体去完成什么事情，就像对外发布一份“合作项目意向书”。对方同意接单后，双方会记录一个Task
   ID，追踪项目进度、交换资料，直到该Task完成为止。
   假设用户让“旅行规划Agent”安排行程，该Agent可通过A2A向“机票预订Agent”发送任务请求，并实时接收状态更新（如“已找到航班，正在比价”）。任务支持即时完成或长达数天的复杂流程，且结果（如生成的行程表）会以标准化格式返回。
3. 跨模态通信  
   协议支持文本、图片、音视频等多种数据类型。例如医疗场景中，影像分析Agent可直接将CT图像传递给诊断Agent，无需中间格式转换。
4. 安全验证机制  
   所有通信默认加密，并通过OAuth等企业级认证，确保只有授权智能体可参与协作，防止数据泄露。

## A2A 的参与者

A2A 协议有三个参与者：

* 用户（User）：使用代理系统完成任务的用户（人类或服务）
* 客户端（Client）：负责转发用户请求的任务
* 服务端（Server）：负责接收客户端的任务，开发者须处理任务请求调用三方LLM API，并响应给客户端

## 开发计划 PLAN

Branches:

* jdk8 support jdk8, SpringBoot 2.7.18
* jdk17 support jdk17, SpringBoot 3.4.5
* main release version of jdk17

Features:

- [x] support spring mvc, reactor, sse
- [x] support servlet and sse
- [x] support webflux and sse
- [x] more a2a4j example project, please refer
  to [a2a4j-examples jdk8](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/jdk8)
  and [a2a4j-examples main](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/main)
- [x] support more LLM, eg.LangChain4j

## a2a4j-examples

[a2a4j-examples](https://github.com/PheonixHkbxoic/a2a4j-examples) is a2a4j SpringBoot example project

### 前置条件

开发环境：JDK8, SpringBoot 2.7.18 please refer
to [a2a4j-examples jdk8](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/jdk8)  
开发环境：JDK17+, SpringBoot 3.4.5 please refer
to [a2a4j-examples main](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/main)

下面的示例开发环境：JDK17+, SpringBoot 3.4.5

### agent/server配置

1. 引入maven依赖

    ```xml
    
    <dependencies>
        <dependency>
            <groupId>io.github.pheonixhkbxoic</groupId>
            <artifactId>a2a4j-agent-mvc-spring-boot-starter</artifactId>
            <version>2.0.0</version>
        </dependency>
        <!-- 或 use webflux -->
        <!--    <dependency>-->
        <!--        <groupId>io.github.pheonixhkbxoic</groupId>-->
        <!--        <artifactId>a2a4j-agent-webflux-spring-boot-starter</artifactId>-->
        <!--        <version>2.0.0</version>-->
        <!--    </dependency>-->
    </dependencies>
    ```

2. 配置AgentCard实例

    ```java
    
    @Bean
    public AgentCard agentCard() {
        AgentCapabilities capabilities = new AgentCapabilities();
        AgentSkill skill = AgentSkill.builder()
                .id("echoAgent")
                .name("echo agent")
                .description("just echo user message")
                .tags(List.of("echo"))
                .examples(Collections.singletonList("I'm big strong!"))
                .inputModes(Collections.singletonList("text"))
                .outputModes(Collections.singletonList("text"))
                .build();
        AgentCard agentCard = new AgentCard();
        agentCard.setName("echoAgent");
        agentCard.setDescription("echo agent, Answer the user's questions exactly as they are");
        agentCard.setUrl("http://127.0.0.1:" + port);
        agentCard.setVersion("2.0.0");
        agentCard.setCapabilities(capabilities);
        agentCard.setSkills(Collections.singletonList(skill));
        return agentCard;
    }
    ```

   AgentCard用来描述当前Agent Server所具有的能力，客户端启动时会连接到
   `http://{your_server_domain}/.well-known/agent.json`
   来获取AgentCard


3. 实现自定义AgentInvoker

    ```java
    
    @Component
    public class EchoAgentInvoker implements AgentInvoker {
        @Resource
        private EchoAgent agent;
    
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
    
    ```

   注意事项：

    * 需要实现`AgentInvoker`接口，处理agent的调用与转换


4. 代码参考  
   [a2a4j-examples agents/echo-agent](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/main/agents/echo-agent)

### host/client配置

1. 引入maven依赖

    ```xml
    
    <dependency>
        <groupId>io.github.pheonixhkbxoic</groupId>
        <artifactId>a2a4j-host-spring-boot-starter</artifactId>
        <version>2.0.0</version>
    </dependency>
    ```

2. 在配置文件(如application.xml)中配置相关属性

    ```yaml
    a2a4j:
      host:
        # can be null
        notification:
          url: http://127.0.0.1:8989/notify
    
        agents:
          agent-001:
            baseUrl: http://127.0.0.1:8901
    
    ```

    * 必须配置Agent Server的baseUrl，可配置多个
    * 选择性的配置Notification Server的baseUrl


3. 发送任务请求并处理响应

    ```java
    
    @Slf4j
    @RestController
    public class AgentController {
        @Resource
        private A2AClientSet clientSet;
    
        @GetMapping("/chat")
        public ResponseEntity<Object> chat(String userId, String sessionId, String prompts) {
            A2AClient client = clientSet.getByConfigKey("echoAgent");
            TaskSendParams params = TaskSendParams.builder()
                    .id(Uuid.uuid4hex())
                    .sessionId(sessionId)
                    .historyLength(3)
                    .acceptedOutputModes(Collections.singletonList("text"))
                    .message(new Message(Role.USER, Collections.singletonList(new TextPart(prompts)), null))
                    .pushNotification(client.getPushNotificationConfig())
                    .build();
            log.info("chat params: {}", Util.toJson(params));
            try {
                String answer = client.sendTask(params)
                        .flatMap(sendTaskResponse -> {
                            if (sendTaskResponse.getError() != null) {
                                return Mono.error(new ValueError(Util.toJson(sendTaskResponse.getError())));
                            }
                            Task task = sendTaskResponse.getResult();
                            return Mono.just(task.getArtifacts().stream()
                                    .flatMap(t -> t.getParts().stream())
                                    .filter(p -> new TextPart().getType().equals(p.getType()))
                                    .map(p -> ((TextPart) p).getText())
                                    .filter(t -> !Util.isEmpty(t))
                                    .collect(Collectors.joining("\n")));
                        })
                        .block();
                return ResponseEntity.ok(answer);
            } catch (A2AClientHTTPError e) {
                log.error("chat exception: {}", e.getMessage(), e);
                return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
            } catch (Exception e) {
                log.error("chat exception: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }
    }
    
    ```

4. 代码参考  
   [a2a4j-examples hosts/standalone](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/main/hosts/standalone)

### notification server配置

1. 引入maven依赖

    ```xml
    
    <dependencies>
    
        <dependency>
            <groupId>io.github.pheonixhkbxoic</groupId>
            <artifactId>a2a4j-notification-mvc-spring-boot-starter</artifactId>
            <version>2.0.0</version>
        </dependency>
        <!-- 或 use webflux -->
        <!--    <dependency>-->
        <!--        <groupId>io.github.pheonixhkbxoic</groupId>-->
        <!--        <artifactId>a2a4j-notification-webflux-spring-boot-starter</artifactId>-->
        <!--        <version>2.0.0</version>-->
        <!--    </dependency>-->
    </dependencies>
    ```

2. 在配置文件(如application.xml)中配置相关属性

    ```yaml
    a2a4j:
      notification:
        # default 
        endpoint: "/notify"
        jwksUrls:
          - http://127.0.0.1:8901/.well-known/jwks.json
    
    ```

    * 必须配置jwksUrls，可配置多个
    * 选择性的配置endpoint, 不配置时默认监听`/notify`, AgentServer中的配置`a2a4j.host.notification:`


3. 自定义监听器并实例化

    ```java
    
    @Component
    public class NotificationListener extends WebMvcNotificationAdapter {
        protected final ScheduledThreadPoolExecutor scheduler;
    
        public NotificationListener(@Autowired A2a4jNotificationProperties a2a4jNotificationProperties) {
            super(a2a4jNotificationProperties.getEndpoint(), a2a4jNotificationProperties.getJwksUrls());
    
            // auto reloadJwks when Agent restart
            scheduler = new ScheduledThreadPoolExecutor(1);
            scheduler.scheduleAtFixedRate(() -> {
                if (verifyFailCount.get() != 0) {
                    this.reloadJwks();
                }
            }, 1, 1, TimeUnit.MINUTES);
        }
        // TODO 实现方法来处理通知，可以使用默认实现
    }
    
    ```

   注意：

    * 需要继承`WebMvcNotificationAdapter`类
    * 注入配置属性`@Autowired A2a4jNotificationProperties a2a4jNotificationProperties`
      并通过`super(a2a4jNotificationProperties.getEndpoint(), a2a4jNotificationProperties.getJwksUrls());`实例化
      `PushNotificationReceiverAuth`和监听指定的地址


4. 代码参考  
   [a2a4j-examples notification-listener](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/main/notification-listener)

## manual

> If you just want to use a2a client manually.

1. 引入maven依赖

    ```xml
    
    <dependency>
        <groupId>io.github.pheonixhkbxoic</groupId>
        <artifactId>a2a4j-core</artifactId>
        <version>2.0.0</version>
    </dependency>
    ```

2. 使用A2AClient连接到Agent Server  
   The agent server must has start

    ```java
    String agentCardBaseUrl = "http://127.0.0.1:8901";
    AgentCard agentCard = new AgentCardResolver(agentCardBaseUrl).resolve();
    A2AClient client = new A2AClient(agentCard);
    
    private static void chat(A2AClient client, String prompts) {
        TaskSendParams params = TaskSendParams.builder()
                .id(Uuid.uuid4hex())
                .sessionId(Uuid.uuid4hex())
                .historyLength(3)
                .acceptedOutputModes(Collections.singletonList("text"))
                .message(new Message(Role.USER, Collections.singletonList(new TextPart(prompts)), null))
                .pushNotification(client.getPushNotificationConfig())
                .build();
        try {
            client.sendTask(params)
                    .flatMap(sendTaskResponse -> {
                        if (sendTaskResponse.getError() != null) {
                            return Mono.error(new ValueError(Util.toJson(sendTaskResponse.getError())));
                        }
                        Task task = sendTaskResponse.getResult();
                        return Mono.just(task.getArtifacts().stream()
                                .flatMap(t -> t.getParts().stream())
                                .filter(p -> new TextPart().getType().equals(p.getType()))
                                .map(p -> ((TextPart) p).getText())
                                .filter(t -> !Util.isEmpty(t))
                                .collect(Collectors.joining("\n")));
                    })
                    .subscribe(System.out::println);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    ```

3. 代码参考  
   [a2a4j-examples hosts cli](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/main/hosts/cli)

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=PheonixHkbxoic/a2a4j&type=Date)](https://www.star-history.com/#PheonixHkbxoic/a2a4j&Date)
