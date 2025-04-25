package io.github.pheonixhkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class PushNotificationNotSupportedError extends JsonRpcError {
    public PushNotificationNotSupportedError() {
        this.setCode(-32003);
        this.setMessage("Push Notification is not supported");
    }
}
