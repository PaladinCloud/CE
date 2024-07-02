package com.paladincloud.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.errors.JobException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapHelper {

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
    public static boolean containsAll(Map<String, ?> m1, Map<String, ?> m2, String[] keys) {
        for (String key : keys) {
            if (!m1.get(key).equals(m2.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the list of non-null values that match the given keys
     *
     * @param map - the object to retrieve values from
     * @param keys - the keys to retrieve
     * @return - a list of non-null String values
     */
    public static List<String> getAllValues(Map<String, Object> map, List<String> keys) {
        var values = new ArrayList<String>();
        for (String key : keys) {
            if (map.containsKey(key)) {
                values.add(map.get(key).toString());
            }
        }
        return values;
    }

    public static String toJsonString(Map<String, ?> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new JobException("Error converting map to json string", e);
        }
    }
}
