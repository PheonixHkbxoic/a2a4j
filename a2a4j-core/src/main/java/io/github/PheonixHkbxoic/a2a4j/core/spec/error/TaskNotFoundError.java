package io.github.PheonixHkbxoic.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 */
public class TaskNotFoundError extends JsonRpcError {
    public TaskNotFoundError() {
        this.setCode(-32001);
        this.setMessage("Task not found");
    }
}
