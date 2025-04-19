package cn.pheker.ai.a2a4j.core.client.sse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/15 19:47
 * @desc
 */
public class SseEventReader {
    private BufferedReader reader;

    public SseEventReader(BufferedReader reader) {
        this.reader = reader;
    }

    public void onEvent(Consumer<SseEvent> consumer, Consumer<IOException> onError) {
        try {
            SseEvent sseEvent = new SseEvent();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // event finish
                if (line.isEmpty()) {
                    sseEvent.setData(sb.toString());
                    consumer.accept(sseEvent);
                    sseEvent = new SseEvent();
                    sb = new StringBuilder();
                }
                int idx = line.indexOf(':');
                if (idx <= 0) {
                    continue;
                }

                String key = line.substring(0, idx), value = line.substring(idx + 1).trim();
                switch (key.trim().toLowerCase()) {
                    case "event":
                        sseEvent.setEvent((value));
                        break;

                    case "data":
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(value.replaceFirst("^ +", ""));
                        break;

                    case "id":
                        sseEvent.setId(value);
                    case "retry":
                        break;

                    default:
                        break;
                }
            }
        } catch (IOException e) {
            onError.accept(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
