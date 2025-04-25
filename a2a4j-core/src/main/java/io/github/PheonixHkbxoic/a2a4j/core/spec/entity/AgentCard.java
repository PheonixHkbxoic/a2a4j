package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;


import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author PheonixHkbxoic
 */
@ToString
@Builder
@Data
public class AgentCard {
    private String name;
    @Nullable
    private String description;
    private String url;
    private AgentProvider provider;
    private String version;
    @Nullable
    private String documentationUrl;
    private AgentCapabilities capabilities;
    @Nullable
    private AgentAuthentication authentication;
    private List<String> defaultInputModes;
    private List<String> defaultOutputModes;
    private List<AgentSkill> skills;

    public AgentCard() {
    }

    public AgentCard(String name, String description, String url, AgentProvider provider, String version, String documentationUrl, AgentCapabilities capabilities, AgentAuthentication authentication, List<String> defaultInputModes, List<String> defaultOutputModes, List<AgentSkill> skills) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.provider = provider;
        this.version = version;
        this.documentationUrl = documentationUrl;
        this.capabilities = capabilities;
        this.authentication = authentication;
        this.defaultInputModes = defaultInputModes;
        this.defaultOutputModes = defaultOutputModes;
        this.skills = skills;
    }
}
