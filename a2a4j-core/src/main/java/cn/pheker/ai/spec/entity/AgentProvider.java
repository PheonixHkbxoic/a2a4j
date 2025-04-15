package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import lombok.Data;

import java.io.Serializable;


@Data
public class AgentProvider implements Serializable {
    private String organization;
    @Nullable
    private String url;
}
