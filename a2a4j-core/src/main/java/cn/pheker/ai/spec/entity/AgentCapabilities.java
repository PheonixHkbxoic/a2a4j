package cn.pheker.ai.spec.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 21:54
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgentCapabilities implements Serializable {
    private boolean streaming;
    private boolean pushNotifications;
    private boolean stateTransitionHistory;
}
