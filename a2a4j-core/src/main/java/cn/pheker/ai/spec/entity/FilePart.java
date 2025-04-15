package cn.pheker.ai.spec.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/10 22:33
 * @desc
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FilePart extends Part implements Serializable {
    private FileContent file;

    public FilePart() {
        this.setType("file");
    }

    public FilePart(String type, Map<String, Object> metadata, FileContent file) {
        super(type, metadata);
        this.file = file;
    }
}
