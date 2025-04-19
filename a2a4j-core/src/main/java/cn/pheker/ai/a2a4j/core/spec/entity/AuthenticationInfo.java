package cn.pheker.ai.a2a4j.core.spec.entity;

import cn.pheker.ai.a2a4j.core.spec.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 23:47
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class AuthenticationInfo implements Serializable {

//    model_config = ConfigDict(extra="allow")

    private List<String> schemes;
    @Nullable
    private String credentials;

}
