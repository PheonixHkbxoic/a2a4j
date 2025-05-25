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
        this.type = Part.FILE;
    }

    public FilePart(FileContent file) {
        this.type = Part.FILE;
        this.file = file;
    }
    
    public FilePart(FileContent file, Map<String, Object> metadata) {
        this.type = Part.FILE;
        this.file = file;
        this.metadata = metadata;
    }
}
