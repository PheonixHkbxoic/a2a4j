package cn.pheker.ai.util;

import java.util.Locale;
import java.util.UUID;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/11 00:09
 * @desc
 */
public class Uuid {

    public static String uuid4hex() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase(Locale.ROOT);
    }

}
