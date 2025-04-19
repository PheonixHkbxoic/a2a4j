package cn.pheker.ai.a2a4j.core.spec.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 21:54
 * @desc
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgentCapabilities implements Serializable {
    private boolean streaming;
    private boolean pushNotifications;
    private boolean stateTransitionHistory;
}
