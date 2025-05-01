package io.github.pheonixhkbxoic.a2a4j.core.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.AgentCard;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author PheonixHkbxoic
 */
public class TestAgentCardDeserialize {
    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void deserialize() throws JsonProcessingException {
        String agentCardJson = "{\"name\":\"Currency Agent\",\"description\":\"current exchange\",\"url\":\"http://localhost:8080/\",\"provider\":null,\"version\":\"1.0.1\",\"documentationUrl\":null,\"capabilities\":{\"streaming\":false,\"pushNotifications\":false,\"stateTransitionHistory\":false},\"authentication\":null,\"defaultInputModes\":null,\"defaultOutputModes\":null,\"skills\":[{\"id\":\"convert_currency\",\"name\":\"Currency Exchange Rates Tool\",\"description\":\"Helps with exchange values between various currencies\",\"tags\":[\"currency conversion\",\"currency exchange\"],\"examples\":[\"What is exchange rate between USD and GBP?\"],\"inputModes\":[\"text\"],\"outputModes\":[\"text\"]}]}";
        AgentCard agentCard = om.readValue(agentCardJson, AgentCard.class);
        Assertions.assertThat(agentCard).isNotNull();
    }
}
