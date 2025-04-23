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

- [x] support jdk8, SpringBoot 2.7.18
- [x] support spring mvc, reactor, sse
- [x] support servlet and sse
- [ ] support more LLM, eg.LangChain
- [ ] support jdk17, SpringBoot 3.X
- [ ] more a2a4j example project

## a2a4j-examples

[a2a4j-examples](https://github.com/PheonixHkbxoic/a2a4j-examples) is a2a4j SpringBoot example project

### 前置条件

* a2a4j目录使用JDK8、SpringBoot 2.7.18进行开发
* 目前支持SpringMvc+Reactor+SSE
* 后续会支持servlet,webflux
* 后续会支持JDK17+, SpringBoot 3.X

### server配置

1. 引入maven依赖

```xml

<dependency>
    <groupId>io.github.pheonixhkbxoic</groupId>
    <artifactId>a2a4j-agent-mvc-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. 配置AgentCard实例

```java

@Bean
public AgentCard agentCard() {
    AgentCapabilities capabilities = new AgentCapabilities();
    AgentSkill skill = AgentSkill.builder().id("convert_currency").name("Currency Exchange Rates Tool").description("Helps with exchange values between various currencies").tags(Arrays.asList("currency conversion", "currency exchange")).examples(Collections.singletonList("What is exchange rate between USD and GBP?")).inputModes(Collections.singletonList("text")).outputModes(Collections.singletonList("text")).build();
    AgentCard agentCard = new AgentCard();
    agentCard.setName("Currency Agent");
    agentCard.setDescription("current exchange");
    agentCard.setUrl("http://127.0.0.1:" + port);
    agentCard.setVersion("1.0.0");
    agentCard.setCapabilities(capabilities);
    agentCard.setSkills(Collections.singletonList(skill));
    return agentCard;
}
```

AgentCard用来描述当前Agent Server所具有的能力，客户端启动时会连接到`http://{your_server_domain}/.well-known/agent.json`
来获取AgentCard

3. 实现自定义任务管理器

```java

@Component
public class EchoTaskManager extends InMemoryTaskManager {
    // wire agent
    private final EchoAgent agent;
    // agent support modes
    private final List<String> supportModes = Arrays.asList("text", "file", "data");

    public EchoTaskManager(@Autowired EchoAgent agent, @Autowired PushNotificationSenderAuth pushNotificationSenderAuth) {
        this.agent = agent;
        // must autowired, keep PushNotificationSenderAuth instance unique global
        this.pushNotificationSenderAuth = pushNotificationSenderAuth;
    }

    @Override
    public SendTaskResponse onSendTask(SendTaskRequest request) {
        log.info("onSendTask request: {}", request);
        TaskSendParams ps = request.getParams();
        // 1. check request params

        // 2. save task

        // 2. agent invoke
        List<Artifact> artifacts = this.agentInvoke(ps).block();

        // 4. save and notification
        Task taskCompleted = this.updateStore(ps.getId(), new TaskStatus(TaskState.COMPLETED), artifacts);
        this.sendTaskNotification(taskCompleted);

        Task taskSnapshot = this.appendTaskHistory(taskCompleted, 3);
        return new SendTaskResponse(taskSnapshot);
    }

    @Override
    public Mono<JsonRpcResponse> onSendTaskSubscribe(SendTaskStreamingRequest request) {
        return Mono.fromCallable(() -> {
            log.info("onSendTaskSubscribe request: {}", request);
            TaskSendParams ps = request.getParams();
            String taskId = ps.getId();

            return null;
        });
    }

    @Override
    public Mono<JsonRpcResponse> onResubscribeTask(TaskResubscriptionRequest request) {
        TaskIdParams params = request.getParams();
        try {
            this.initEventQueue(params.getId(), true);
        } catch (Exception e) {
            log.error("Error while reconnecting to SSE stream: {}", e.getMessage());
            return Mono.just(new JsonRpcResponse(request.getId(), new InternalError("An error occurred while reconnecting to stream: " + e.getMessage())));
        }
        return Mono.empty();
    }

    // simulate agent invoke
    private Mono<List<Artifact>> agentInvoke(TaskSendParams ps) {

    }

    private JsonRpcResponse<Object> validRequest(JsonRpcRequest<TaskSendParams> request) {

    }
}

```

注意事项：

* 需要继承`InMemoryTaskManager`类，并实现必要的方法，如：`onSendTask`,`onSendTaskSubscribe`
* 需要注入`PushNotificationSenderAuth`实例，如果想发送任务状态通知到通知监听服务器，此实例会通过
  ``http://{your_server_domain}/.well-known/jwks.json``来开放公钥，通知监听器通过`PushNotificationReceiverAuth`
  来验证通知请求token、数据是否有效
* 自定义类Agent来与底层LLM交互

4. 代码参考
   [a2a4j-examples agents/echo-agent](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/main/agents/echo-agent)

### client/host配置

1. 引入maven依赖

```xml

<dependency>
    <groupId>io.github.pheonixhkbxoic</groupId>
    <artifactId>a2a4j-host-spring-boot-starter</artifactId>
    <version>1.0.0</version>
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
    private List<A2AClient> clients;

    @GetMapping("/chat")
    public ResponseEntity<Object> chat(String userId, String sessionId, String prompts) {
        A2AClient client = clients.get(0);
        TaskSendParams params = TaskSendParams.builder()
                .id(Uuid.uuid4hex())
                .sessionId(sessionId)
                .historyLength(3)
                .acceptedOutputModes(Collections.singletonList("text"))
                .message(new Message(Role.USER, Collections.singletonList(new TextPart(prompts)), null))
                .pushNotification(client.getPushNotificationConfig())
                .build();
        log.info("params: {}", Util.toJson(params));
        SendTaskResponse sendTaskResponse = client.sendTask(params);

        JsonRpcError error = sendTaskResponse.getError();
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }
        Task task = sendTaskResponse.getResult();
        String answer = task.getArtifacts().stream()
                .flatMap(t -> t.getParts().stream())
                .filter(p -> new TextPart().getType().equals(p.getType()))
                .map(p -> ((TextPart) p).getText())
                .collect(Collectors.joining("\n"));
        return ResponseEntity.ok(answer);
    }

}

```

4. 代码参考
   [a2a4j-examples hosts/standalone](https://github.com/PheonixHkbxoic/a2a4j-examples/tree/main/hosts/standalone)

### notification server配置

1. 引入maven依赖

```xml

<dependency>
    <groupId>io.github.pheonixhkbxoic</groupId>
    <artifactId>a2a4j-notification-mvc-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
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
* 选择性的配置endpoint, 不配置时默认监听`/notify`

3. 自定义监听器并实例化

```java

@Component
public class NotificationListener extends WebMvcNotificationAdapter {

    public NotificationListener(@Autowired A2a4jNotificationProperties a2a4jNotificationProperties) {
        super(a2a4jNotificationProperties.getEndpoint(), a2a4jNotificationProperties.getJwksUrls());
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

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=PheonixHkbxoic/a2a4j&type=Date)](https://www.star-history.com/#PheonixHkbxoic/a2a4j&Date)
