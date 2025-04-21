package io.github.PheonixHkbxoic.a2a4j.core.spec.message;

import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.TaskQueryParams;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
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
