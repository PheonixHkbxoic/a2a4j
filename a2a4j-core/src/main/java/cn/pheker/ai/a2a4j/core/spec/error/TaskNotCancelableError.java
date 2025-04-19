package cn.pheker.ai.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class TaskNotCancelableError extends JsonRpcError {
    public TaskNotCancelableError() {
        this.setCode(-32002);
        this.setMessage("Task cannot be canceled");
    }
}
