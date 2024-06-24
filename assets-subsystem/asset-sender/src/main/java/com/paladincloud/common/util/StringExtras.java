package com.paladincloud.common.util;

import com.paladincloud.common.config.ConfigConstants.Dev;
import com.paladincloud.common.config.ConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringExtras {

    public static final String[] EMPTY_ARRAY = new String[0];
    private static final Logger LOGGER = LogManager.getLogger(StringExtras.class);

    private StringExtras() {
    }

    public static String indexName(String dataSource, String type) {
        var name = STR."\{dataSource}_\{type}";
        var devPrefix = ConfigService.get(Dev.INDEX_PREFIX);
        if (!StringUtils.isEmpty(devPrefix)) {
            name = STR."\{devPrefix}-\{name}";
            LOGGER.warn("Overriding index prefix with '{}' => {}", devPrefix, name);
        }
        return name;
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
