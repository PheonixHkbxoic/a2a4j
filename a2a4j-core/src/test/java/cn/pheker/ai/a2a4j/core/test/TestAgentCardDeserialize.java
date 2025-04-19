package cn.pheker.ai.a2a4j.core.test;

import cn.pheker.ai.a2a4j.core.spec.entity.AgentCard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.Asserts;
import org.junit.jupiter.api.Test;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/12 20:27
 * @desc
 */
public class TestAgentCardDeserialize {
    private ObjectMapper om = new ObjectMapper();
    private String agentCardJson = "{\"name\":\"Currency Agent\",\"description\":\"current exchange\",\"url\":\"http://localhost:8080/\",\"provider\":null,\"version\":\"1.0.0\",\"documentationUrl\":null,\"capabilities\":{\"streaming\":false,\"pushNotifications\":false,\"stateTransitionHistory\":false},\"authentication\":null,\"defaultInputModes\":null,\"defaultOutputModes\":null,\"skills\":[{\"id\":\"convert_currency\",\"name\":\"Currency Exchange Rates Tool\",\"description\":\"Helps with exchange values between various currencies\",\"tags\":[\"currency conversion\",\"currency exchange\"],\"examples\":[\"What is exchange rate between USD and GBP?\"],\"inputModes\":[\"text\"],\"outputModes\":[\"text\"]}]}";

    @Test
    public void deserialize() throws JsonProcessingException {
        AgentCard agentCard = om.readValue(agentCardJson, AgentCard.class);
        Asserts.notNull(agentCard, "agent card deserialize failed");
    }
}
