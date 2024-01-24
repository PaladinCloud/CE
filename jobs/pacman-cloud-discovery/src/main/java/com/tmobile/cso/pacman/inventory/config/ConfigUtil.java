package com.tmobile.cso.pacman.inventory.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.inventory.util.InventoryConstants;
import com.tmobile.cso.pacman.inventory.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {
    private static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);
    private static final String CONFIG_URL = System.getenv("CONFIG_URL");

    private ConfigUtil() {
    }

    public static void setConfigProperties() throws Exception {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.putAll(fetchConfigProperties());
        System.setProperties(properties);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> fetchConfigProperties() throws Exception {
        Map<String, String> properties = new HashMap<>();
        String base64Creds = System.getProperty("config_creds");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, String> appProps = new HashMap<>();
            Map<String, String> batchProps = new HashMap<>();
            Map<String, String> invProps = new HashMap<>();
            Map<String, Object> response = objectMapper.readValue(Util.httpGetMethodWithHeaders(CONFIG_URL, Util.getHeader(base64Creds)), new TypeReference<Map<String, Object>>() {
            });
            List<Map<String, Object>> propertySources = (List<Map<String, Object>>) response.get("propertySources");
            for (Map<String, Object> propertySource : propertySources) {
                if (propertySource.get(InventoryConstants.NAME).toString().contains(InventoryConstants.APPLICATION)) {
                    appProps.putAll((Map<String, String>) propertySource.get(InventoryConstants.SOURCE));
                }
                if (propertySource.get(InventoryConstants.NAME).toString().contains(InventoryConstants.BATCH)) {
                    batchProps.putAll((Map<String, String>) propertySource.get(InventoryConstants.SOURCE));
                }
                if (propertySource.get(InventoryConstants.NAME).toString().contains(InventoryConstants.INVENTORY)) {
                    invProps.putAll((Map<String, String>) propertySource.get(InventoryConstants.SOURCE));
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
            throw new Exception("No config properties fetched from " + CONFIG_URL);
        }

        log.info("Config are fetched from {}", CONFIG_URL);
        return properties;
    }
}
