package com.paladincloud.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.util.HttpHelper;
import com.paladincloud.common.util.HttpHelper.AuthorizationType;
import java.util.Map;
import java.util.Properties;

public class ConfigService {

    private static final Properties properties = new Properties();

    private ConfigService() {
    }

    public static String get(String propertyName) {
        return get(propertyName, null);
    }

    public static String get(String propertyName, String defaultValue) {
        return properties.getProperty(propertyName, defaultValue);
    }


    /**
     * Retrieve configuration properties from the given config service, making them available in the
     * properties field.
     * <p></p>
     * NOTE: Retrieved properties are prefixed, ie, 'elastic-search.host', which comes from the
     * 'batch' source, becomes 'batch.elastic-search.host'
     *
     * @param configUrl         - the URL to the config service
     * @param configCredentials - the BASE64 encoded credentials
     */
    public static void retrieveConfigProperties(String configUrl, String configCredentials) {
        if (!properties.isEmpty()) {
            return;
        }
        fetchConfigProperties(configUrl, configCredentials);
    }

    public static void setProperties(String prefix, Map<String, String> values) {
        for (var entry : values.entrySet()) {
            properties.put(prefix + entry.getKey(), entry.getValue());
        }
    }

    private static void fetchConfigProperties(String uri, String credentials) {
        var headers = HttpHelper.getBasicHeaders(AuthorizationType.BASIC, credentials);
        try {
            var configJson = HttpHelper.get(uri, headers);
            var objectMapper = new ObjectMapper().configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            var configResponse = objectMapper.readValue(configJson, ConfigApiResponse.class);
            for (var source : configResponse.propertySources) {
                String prefix = null;
                if (source.name.contains("application")) {
                    prefix = "application";
                } else if (source.name.contains("batch")) {
                    prefix = "batch";
                }
                if (prefix != null) {
                    for (var entry : source.source.entrySet()) {
                        properties.put(STR."\{prefix}.\{entry.getKey()}", entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            throw new JobException("Unable to get configuration properties", e);
        }
    }
}
