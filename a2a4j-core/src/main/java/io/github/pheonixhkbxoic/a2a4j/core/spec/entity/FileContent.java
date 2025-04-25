package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
import io.github.pheonixhkbxoic.a2a4j.core.spec.ValueError;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author PheonixHkbxoic
 */
@ToString
@Data
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
