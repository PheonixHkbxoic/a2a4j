package io.github.pheonixhkbxoic.a2a4j.core.util;

import java.util.Locale;
import java.util.UUID;

/**
 * @author PheonixHkbxoic
 */
public class Uuid {

    public static String uuid4hex() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase(Locale.ROOT);
    }

}
