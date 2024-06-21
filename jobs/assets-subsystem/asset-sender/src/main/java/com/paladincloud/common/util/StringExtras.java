package com.paladincloud.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class StringExtras {

    public static final String[] EMPTY_ARRAY = new String[0];

    private StringExtras() {
    }

    public static String indexName(String dataSource, String type) {
        System.out.println("!! DON'T FORGET TO REMOVE THIS INDEX NAME HACK KEVIN!!");
        return STR."kvt_\{dataSource}_\{type}";
//        return STR."\{dataSource}_\{type}";
    }

    /**
     * Concatenate.
     *
     * @param map       the map
     * @param keys      the keys
     * @param delimiter the delimiter
     * @return the string
     */
    public static String concatenate(Map<String, Object> map, String[] keys, String delimiter) {
        List<String> values = new ArrayList<>();
        for (String key : keys) {
            if (map.get(key) == null) {
                continue;
            }
            values.add(map.get(key).toString());
        }

        return String.join(delimiter, values);
    }

    public static String[] split(String str, String delimiter, String[] defaultValue) {
        if (str != null) {
            return StringUtils.split(str, delimiter);
        }
        return defaultValue;
    }
}
