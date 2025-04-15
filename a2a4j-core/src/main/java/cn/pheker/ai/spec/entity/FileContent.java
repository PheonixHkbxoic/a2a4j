package cn.pheker.ai.spec.entity;

import cn.pheker.ai.spec.Nullable;
import cn.pheker.ai.spec.ValueError;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:34
 * @desc
 */
public class FileContent implements Serializable {
    @Nullable
    private String name;
    @Nullable
    private String mimeType;
    @Nullable
    private String bytes;
    @Nullable
    private String uri;

    //    @model_validator(mode="after")
    public void checkContent() {
        if (this.bytes != null && uri != null) {
            throw new ValueError("Either 'bytes' or 'uri' must be present in the file data");
        }
        if (this.bytes == null && uri == null) {
            throw new ValueError("Only one of 'bytes' or 'uri' can be present in the file data");
        }
    }

}
