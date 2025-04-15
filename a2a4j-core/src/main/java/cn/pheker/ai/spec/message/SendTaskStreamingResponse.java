package cn.pheker.ai.spec.message;

import cn.pheker.ai.spec.entity.UpdateEvent;
import cn.pheker.ai.spec.error.JsonRpcError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 13:26
 * @desc
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Data
public class SendTaskStreamingResponse extends JsonRpcResponse<UpdateEvent> {
    public SendTaskStreamingResponse() {
    }

    public SendTaskStreamingResponse(String id, UpdateEvent updateEvent) {
        this.setId(id);
        this.setResult(updateEvent);
    }

    public SendTaskStreamingResponse(String id, JsonRpcError error) {
        super(id, error);
    }
}
