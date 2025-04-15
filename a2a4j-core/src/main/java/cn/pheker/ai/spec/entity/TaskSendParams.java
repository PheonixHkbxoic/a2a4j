package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 23:30
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TaskSendParams {
    /**
     * task id
     */
    private String id;
    // uuid 4 hex
    private String sessionId;
    private Message message;
    @Nullable
    private List<String> acceptedOutputModes;
    @Nullable
    private PushNotificationConfig pushNotification;
    @Nullable
    private Integer historyLength;
    @Nullable
    private Map<String, Object> metadata;

}
