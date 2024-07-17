package com.paladincloud.common.util;

import com.paladincloud.common.config.ConfigConstants.Dev;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringHelper {

    public static final String[] EMPTY_ARRAY = new String[0];
    private static final Logger LOGGER = LogManager.getLogger(StringHelper.class);
    private static boolean hasWarnedIndexOverride = false;

    private StringHelper() {
    }

    public static String indexName(String dataSource, String type) {
        var name = STR."\{dataSource}_\{type}";
        var devPrefix = ConfigService.get(Dev.INDEX_PREFIX);
        if (!StringUtils.isEmpty(devPrefix)) {
            name = STR."\{devPrefix}_\{name}";
            if (!hasWarnedIndexOverride) {
                LOGGER.warn("Overriding index prefix with '{}' => {}", devPrefix, name);
                hasWarnedIndexOverride = true;
            }
        }
        return name;
    }

    /**
     * Concatenate all matching keys in 'map', ignoring null values.
     *
     * @param map       the map
     * @param keys      the keys
     * @param delimiter the delimiter
     * @return - the concatenated values as a string
     */
    public static String concatenate(Map<String, Object> map, List<String> keys, String delimiter) {
        return keys.stream()
            .map(map::get)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.joining(delimiter));
    }

    /**
     * Split a string, returning the default value if the string is null
     *
     * @param str          - the string to split
     * @param delimiter    - the delimiter to split on
     * @param defaultValue - the default value to return if the string is null
     * @return - A string array that are the split values
     */
    public static String[] split(String str, String delimiter, String[] defaultValue) {
        if (str != null) {
            return StringUtils.split(str, delimiter);
        }
        return defaultValue;
    }

    /**
     * Given a string, generate a signature for it
     *
     * @param doc - the string to process
     * @return - a string with the signature for `doc`
     */
    public static String generateSignature(String doc) {
        try {
            return Hex.encodeHexString(MessageDigest.getInstance("MD5").digest(doc.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new JobException("Unable to generate unique ID", e);
        }
    }
}
