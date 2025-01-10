package com.howdev.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JacksonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonUtil.class);
    private static final String DATE_PATTERN = "yyyyMMddHHmmss";
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JacksonUtil() {
        // no need to do.
    }

    /**
     * 创建一个自定义的JSON ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setDateFormat(new SimpleDateFormat(DATE_PATTERN));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        return objectMapper;
    }

    public static ObjectWriter getWriter() {
        return createObjectMapper().writer();
    }

    public static ObjectReader getReader() {
        return createObjectMapper().reader();
    }

    /**
     * 将对象转换为JSON字符串
     */
    public static <T> String toJson(T value) {
        if (value == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        JsonGenerator gen = null;
        try {
            gen = JSON_FACTORY.createGenerator(sw);
            OBJECT_MAPPER.writeValue(gen, value);
            return sw.toString();
        } catch (IOException e) {
            LOGGER.error("object to json exception!", e);
        } finally {
            if (gen != null) {
                try {
                    gen.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception occurred when closing JSON generator!", e);
                }
            }
        }
        return null;
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        try {
            return getReader().forType(clazz).readValue(json);
        } catch (IOException e) {
            LOGGER.error("json to object exception!", e);
            throw new Exception("json to object exception:" + json);
        }
    }

    /**
     * 将JSON字符串转换为指定对象
     */
    public static <T> T toObject(String jsonString, Class<T> valueType, Class<?>... itemTypes) throws Exception {
        if (Objects.isNull(jsonString) || jsonString.isEmpty()) {
            return null;
        }
        try {
            if (itemTypes.length == 0) {
                // 非集合类型
                return OBJECT_MAPPER.readValue(jsonString, valueType);
            } else {
                // 集合类型, 如List,Map
                JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(valueType, itemTypes);
                return OBJECT_MAPPER.readValue(jsonString, javaType);
            }
        } catch (Exception e) {
            LOGGER.error("json to object exception! string={}", jsonString, e);
            throw new Exception("json to object exception:" + jsonString);
        }
    }

    /**
     * 自定义TypeReference，解决复杂嵌套结构的转换
     */
    public static <T> T toObject(String jsonString, TypeReference<T> type) throws Exception {
        if ((Objects.isNull(jsonString) || jsonString.isEmpty()) && type == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonString, type);
        } catch (Exception e) {
            LOGGER.error("json to object exception!", e);
            throw new Exception("json to object exception:" + jsonString);
        }
    }

    /**
     * json字符串转换为 hashMap
     *
     * @param json       json字符串
     * @param keyClazz   map key 的对象格式
     * @param valueClazz map value 的对象格式
     * @param <K>        map - key
     * @param <V>        map - value
     * @return 转换后的map对象
     */
    public static <K, V> Map<K, V> toHashMap(String json, Class<K> keyClazz, Class<V> valueClazz) throws Exception {
        if (Objects.isNull(json) || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(
                    json, TypeFactory.defaultInstance().constructMapType(HashMap.class, keyClazz, valueClazz)
            );
        } catch (Exception e) {
            LOGGER.error("json to map exception! string={}", json, e);
            throw new Exception("json to map exception");
        }
    }
}