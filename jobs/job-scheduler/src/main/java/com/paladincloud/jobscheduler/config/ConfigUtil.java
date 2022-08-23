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

    private static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    private static final String configUrl = System.getenv("CONFIG_URL");

    public static void setConfigProperties() throws Exception {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());

        Map<String, String> configProps = fetchConfigProperties();
        // set the config property values
        properties.putAll(configProps);
        System.setProperties(properties);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> fetchConfigProperties() throws Exception {

        Map<String, String> properties = new HashMap<>();
        String base64Creds = System.getenv(Constants.CONFIG_CREDS);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, String> jobSchedulerProps = new HashMap<>();
            Map<String, Object> response = objectMapper.readValue(Util.httpGetMethodWithHeaders(configUrl, Util.getHeader(base64Creds)), new TypeReference<Map<String, Object>>() {
            });
            List<Map<String, Object>> propertySources = (List<Map<String, Object>>) response.get("propertySources");
            for (Map<String, Object> propertySource : propertySources) {
                if (propertySource.get(Constants.NAME).toString().contains(Constants.JOB_SCHEDULER)) {
                    jobSchedulerProps.putAll((Map<String, String>) propertySource.get(Constants.SOURCE));
                }

                properties.putAll(jobSchedulerProps);
            }
        } catch (Exception e) {
            log.error("Error in fetchConfigProperties", e);
            throw e;
        }
        if (properties.isEmpty()) {
            throw new Exception("No config properties fetched from " + configUrl);
        }

        log.info("Config are fetched from {}", configUrl);
        properties.forEach((k, v) -> log.debug("   {} : {} ", k, v));
        return properties;
    }
}
