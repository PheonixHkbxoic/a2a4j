package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 23:47
 * @desc
 */
@Data
public class AuthenticationInfo implements Serializable {

//    model_config = ConfigDict(extra="allow")

    private List<String> schemes;
    @Nullable
    private String credentials;

}
