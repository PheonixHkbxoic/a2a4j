package io.github.pheonixhkbxoic.a2a4j.core.spec;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/30 16:06
 * @desc all support methods
 */
public class Method {
    public static String TASKS_GET = "tasks/get";
    public static String TASKS_SEND = "tasks/send";
    public static String TASKS_CANCEL = "tasks/cancel";
    public static String TASKS_SENDSUBSCRIBE = "tasks/sendSubscribe";
    public static String TASKS_RESUBSCRIBE = "tasks/resubscribe";
    public static String TASKS_PUSHNOTIFICATION_SET = "tasks/pushNotification/set";
    public static String TASKS_PUSHNOTIFICATION_GET = "tasks/pushNotification/get";

    public static boolean isSse(String method) {
        return Method.TASKS_SENDSUBSCRIBE.equalsIgnoreCase(method) || Method.TASKS_RESUBSCRIBE.equalsIgnoreCase(method);
    }
}
