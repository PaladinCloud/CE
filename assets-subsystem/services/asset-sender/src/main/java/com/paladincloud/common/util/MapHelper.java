package com.paladincloud.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.errors.JobException;
import java.util.List;
import java.util.Map;

public class MapHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MapHelper() {
    }

    /**
     * Ensure all 'keys' have equal values in both maps
     *
     * @param m1   the first map
     * @param m2   the second map
     * @param keys the keys to check
     * @return true, if all keys have the same value, false otherwise
     */
    public static boolean containsAll(Map<String, ?> m1, Map<String, ?> m2, List<String> keys) {
        for (String key : keys) {
            if (!m1.get(key).equals(m2.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ensure all 'keys' have equal values in both maps
     *
     * @param m1   the first map
     * @param m2   the second map
     * @param keys the keys to check
     * @return true, if all keys have the same value, false otherwise
     */
    public static boolean containsAll(Map<String, ?> m1, Map<String, ?> m2, String[] keys) {
        for (String key : keys) {
            if (!m1.get(key).equals(m2.get(key))) {
                return false;
            }
        }
        return true;
    }

    public static String toJsonString(Map<String, ?> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new JobException("Error converting map to json string", e);
        }
    }
}
