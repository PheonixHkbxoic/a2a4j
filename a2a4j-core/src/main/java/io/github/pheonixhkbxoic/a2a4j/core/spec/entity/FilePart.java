package io.github.pheonixhkbxoic.a2a4j.core.spec.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class FilePart extends Part implements Serializable {
    @Valid
    @NotNull
    private FileContent file;

    public FilePart() {
        super.setType("file");
    }

    public FilePart(String type, Map<String, Object> metadata, FileContent file) {
        super(type, metadata);
        this.file = file;
    }
}
