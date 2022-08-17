package com.paladincloud.jobscheduler.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.jobscheduler.util.Constants;
import com.paladincloud.jobscheduler.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {

    private static Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    private static String configUrl = System.getenv("CONFIG_URL");

    public static void setConfigProperties() throws Exception {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());

        Map<String, String> configProps = fetchConfigProperties();
        // set the config property values
        for (Map.Entry<String, String> entry : configProps.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("s3.role") || entry.getKey().equalsIgnoreCase("base.region") || entry.getKey().equalsIgnoreCase("base.account")) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        System.setProperties(properties);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> fetchConfigProperties() throws Exception {

        Map<String, String> properties = new HashMap<>();
        String base64Creds = System.getenv(Constants.CONFIG_CREDS);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, String> appProps = new HashMap<>();
            Map<String, String> batchProps = new HashMap<>();
            Map<String, String> invProps = new HashMap<>();
            Map<String, Object> response = objectMapper.readValue(Util.httpGetMethodWithHeaders(configUrl, Util.getHeader(base64Creds)), new TypeReference<Map<String, Object>>() {
            });
            List<Map<String, Object>> propertySources = (List<Map<String, Object>>) response.get("propertySources");
            for (Map<String, Object> propertySource : propertySources) {
                if (propertySource.get(Constants.NAME).toString().contains(Constants.APPLICATION)) {
                    appProps.putAll((Map<String, String>) propertySource.get(Constants.SOURCE));
                }
                if (propertySource.get(Constants.NAME).toString().contains(Constants.BATCH)) {
                    batchProps.putAll((Map<String, String>) propertySource.get(Constants.SOURCE));
                }
                if (propertySource.get(Constants.NAME).toString().contains(Constants.INVENTORY)) {
                    invProps.putAll((Map<String, String>) propertySource.get(Constants.SOURCE));
                }
                properties.putAll(appProps);
                properties.putAll(batchProps);
                properties.putAll(invProps);
            }
        } catch (Exception e) {
            log.error("Error in fetchConfigProperties", e);
            throw e;
        }
        if (properties.isEmpty()) {
            throw new Exception("No config properties fetched from " + configUrl);
        }

        log.info("Config are feteched from {}", configUrl);
        properties.forEach((k, v) -> log.debug("   {} : {} ", k, v));
        return properties;
    }
}
