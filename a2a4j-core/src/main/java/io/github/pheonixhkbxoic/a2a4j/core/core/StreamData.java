package io.github.pheonixhkbxoic.a2a4j.core.core;

import io.github.pheonixhkbxoic.a2a4j.core.spec.Nullable;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Artifact;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Message;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.TaskState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/30 23:36
 * @desc
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class StreamData {
    private TaskState state;
    @Nullable
    private Message message;
    @Nullable
    private Artifact artifact;
    /**
     * indicate stream whether end
     * so the endStream of last StreamData must be true
     */
    private boolean endStream;
}
