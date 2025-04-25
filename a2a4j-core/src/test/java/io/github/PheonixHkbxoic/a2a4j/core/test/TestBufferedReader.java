package io.github.pheonixhkbxoic.a2a4j.core.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author PheonixHkbxoic
 */
public class TestBufferedReader {


    public static void main(String[] args) {
        String json = "event: custom-event\ndata: {\"key\": \"value\"}\nid: 12345\\nretry: 5000\n\n";

        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        BufferedReader buf = new BufferedReader(reader);
        try {
            String line;
            while ((line = buf.readLine()) != null) {
                System.out.println("line = " + line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
