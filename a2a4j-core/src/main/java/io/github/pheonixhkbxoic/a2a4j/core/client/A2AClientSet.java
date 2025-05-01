package io.github.pheonixhkbxoic.a2a4j.core.client;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/28 12:59
 * @desc
 */
public class A2AClientSet {
    private Map<String, A2AClient> clientMap;

    public A2AClientSet(Map<String, A2AClient> clientMap) {
        this.clientMap = clientMap;
    }

    public A2AClient getByConfigKey(String key) {
        return clientMap.get(key);
    }

    public Set<String> keySet() {
        return clientMap.keySet();
    }

    public A2AClient getByName(String name) {
        return clientMap.values().stream()
                .filter(c -> c.getAgentCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public Map<String, A2AClient> toNameMap() {
        return clientMap.values().stream().collect(Collectors.toMap(c -> c.getAgentCard().getName(), Function.identity()));
    }

    public int size() {
        return clientMap.size();
    }

    public boolean isEmpty() {
        return clientMap.isEmpty();
    }
    
}
