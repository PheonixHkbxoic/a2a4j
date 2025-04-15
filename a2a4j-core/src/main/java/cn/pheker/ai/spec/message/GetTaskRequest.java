package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.entity.TaskQueryParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:17
 * @desc
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Data
public class GetTaskRequest extends JsonRpcRequest<TaskQueryParams> {
    public GetTaskRequest() {
        this.setMethod("tasks/get");
    }

    public GetTaskRequest(TaskQueryParams params) {
        this();
        this.setParams(params);
    }
}
