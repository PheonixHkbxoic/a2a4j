package io.github.pheonixhkbxoic.a2a4j.core.util;

import io.github.pheonixhkbxoic.a2a4j.core.spec.error.ContentTypeNotSupportedError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.error.UnsupportedOperationError;
import io.github.pheonixhkbxoic.a2a4j.core.spec.message.JsonRpcResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author PheonixHkbxoic
 */
@Slf4j
public class Util {
    public static boolean isEmpty(CharSequence c) {
        return c == null || c.length() == 0;
    }

    public static boolean isEmpty(Collection c) {
        return c == null || c.isEmpty();
    }


    public static boolean areModalitiesCompatible(List<String> serverOutputModes, List<String> clientOutputModes) {
        if (serverOutputModes == null || serverOutputModes.isEmpty()) {
            return true;
        }
        if (clientOutputModes == null || clientOutputModes.isEmpty()) {
            return true;
        }
        return new ArrayList<>(clientOutputModes).retainAll(serverOutputModes);
    }

    public static <T> JsonRpcResponse<T> newIncompatibleTypesError(String requestId) {
        return new JsonRpcResponse<>(requestId, new ContentTypeNotSupportedError());
    }

    public static <T> JsonRpcResponse<T> newNotImplementedError(String requestId) {
        return new JsonRpcResponse<>(requestId, new UnsupportedOperationError());
    }


    private static ObjectMapper objectMapper = new ObjectMapper();

//    static {
//        //这个特性，决定了解析器是否将自动关闭那些不属于parser自己的输入源。
//// 如果禁止，则调用应用不得不分别去关闭那些被用来创建parser的基础输入流InputStream和reader；
////默认是true
//        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
////是否允许解析使用Java/C++ 样式的注释（包括'/'+'*' 和'//' 变量）
//        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
//
//
////是否允许单引号来包住属性名称和字符串值
//        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
//
////是否允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）
//        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
//
////是否允许JSON整数以多个0开始
//        objectMapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
//
////null的属性不序列化
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//
////按字母顺序排序属性,默认false
//        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
//
////是否缩放排列输出,默认false
//        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
//
////序列化Date日期时以timestamps输出，默认true
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
//
////序列化枚举是否以toString()来输出，默认false，即默认以name()来输出
//        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
//
////序列化枚举是否以ordinal()来输出，默认false
//        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, false);
//
////序列化单元素数组时不以数组来输出，默认false
//        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
//
////序列化Map时对key进行排序操作，默认false
//        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
//
////序列化char[]时以json数组输出，默认false
//        objectMapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
//

    /// /序列化BigDecimal时是输出原始数字还是科学计数，默认false，即以toPlainString()科学计数方式来输出
//        objectMapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
//    }
//
    public static <T> T deepCopyJson(T object, Class<T> tClass) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("object json exception: {}", e.getMessage());
            return "";
        }
    }

    public static <T> T fromJson(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String json = "{\"jsonrpc\":\"2.0\",\"id\":\"4cfc6c25a616401ba5095e72475181c2\",\"method\":\"tasks/sendSubscribe\",\"params\":{\"id\":\"e6d4a0a98055472bb3b6184bfc16fdfb\",\"sessionId\":\"15bd8990773f45d58a88a3c1b5865043\",\"message\":{\"role\":\"user\",\"parts\":[{\"type\":\"text\",\"type\":\"text\",\"metadata\":null,\"text\":\"100块人民币能总汇多少美元\"}],\"metadata\":null},\"acceptedOutputModes\":null,\"pushNotification\":null,\"historyLength\":3,\"metadata\":null}}\n";

    public static void main(String[] args) {
        JsonRpcResponse jsonRpcResponse = Util.fromJson(json, JsonRpcResponse.class);
        System.out.println("jsonRpcResponse = " + jsonRpcResponse);
    }
}
