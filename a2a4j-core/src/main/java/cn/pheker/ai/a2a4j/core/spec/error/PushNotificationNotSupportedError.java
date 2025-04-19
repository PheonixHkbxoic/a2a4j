package cn.pheker.ai.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class PushNotificationNotSupportedError extends JsonRpcError {
    public PushNotificationNotSupportedError() {
        this.setCode(-32003);
        this.setMessage("Push Notification is not supported");
    }
}
