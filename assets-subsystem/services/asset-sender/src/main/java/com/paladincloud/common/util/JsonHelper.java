package com.paladincloud.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;

public class JsonHelper {

    public static final ObjectMapper objectMapper = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule());

    private JsonHelper() {
    }

    /**
     * Converts the JSON string to an instance of the specified class. Unknown properties will be
     * ignored
     *
     * @param clazz - the class to transform the JSON to
     * @param json  - the JSON string to convert
     * @param <T>   - the Java object to map the JSON onto
     * @return - an instance of `clazz`
     * @throws JsonProcessingException - Any JSON parsing errors
     */
    public static <T> T fromString(Class<T> clazz, String json) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    public static Map<String, Object> mapFromString(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
