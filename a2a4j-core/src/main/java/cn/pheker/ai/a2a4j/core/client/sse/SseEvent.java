package cn.pheker.ai.a2a4j.core.client.sse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/15 17:37
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class SseEvent {
    private String id;
    private String event;
    private String data;

    public boolean hasData() {
        return data != null && !data.isEmpty();
    }
}