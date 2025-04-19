package cn.pheker.ai.a2a4j.core.spec.error;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 19:28
 * @desc
 */
public class TaskNotFoundError extends JsonRpcError {
    public TaskNotFoundError() {
        this.setCode(-32001);
        this.setMessage("Task not found");
    }
}
