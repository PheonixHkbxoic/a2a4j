package io.github.PheonixHkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class TaskNotCancelableError extends JsonRpcError {
    public TaskNotCancelableError() {
        this.setCode(-32002);
        this.setMessage("Task cannot be canceled");
    }
}
