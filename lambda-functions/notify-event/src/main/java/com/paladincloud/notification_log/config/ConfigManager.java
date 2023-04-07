package com.paladincloud.notification_log.config;

import java.util.Hashtable;
import java.util.Map;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.paladincloud.notification_log.common.Constants;
import com.paladincloud.notification_log.util.CommonHttpUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigManager {



    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    /**
     * Gets the configurations map.
     *
     * @return the configurations map
     */
    public static Hashtable<String, Object> getConfigurationsMap() {

        JsonArray propertySourcesArray = new JsonArray();
        Hashtable<String, Object> appPropsHashtable = new Hashtable<>();
        Hashtable<String, Object> rulePropsHashtable = new Hashtable<>();
        Hashtable<String, Object> configHashtable = new Hashtable<>();

        String configServerURL = System.getenv(Constants.CONFIG_SERVICE_URL);
        String configCredentials = System.getenv(Constants.CONFIG_CREDENTIALS);

        if (configServerURL == null || configServerURL.isEmpty() ||
                configCredentials == null || configCredentials.isEmpty()) {
            logger.info(Constants.MISSING_CONFIGURATION);
            throw  new RuntimeException(Constants.MISSING_CONFIGURATION);
        }

        Map<String, Object> configCreds = CommonHttpUtils.getHeader(configCredentials);

        JsonObject configurationsFromPacmanTable = CommonHttpUtils.getConfigurationsFromConfigApi(configServerURL, configCreds);
        logger.info("Configured values {} ",configurationsFromPacmanTable);
        if (configurationsFromPacmanTable != null) {
            propertySourcesArray = configurationsFromPacmanTable.get("propertySources").getAsJsonArray();
        }


        if (propertySourcesArray.size() > 0) {
            for (int i = 0; i < propertySourcesArray.size(); i++) {
                JsonObject propertySource = (JsonObject) propertySourcesArray.get(i);

                if (propertySource.get(Constants.NAME).toString().contains("application")) {
                    JsonObject appProps = propertySource.get(Constants.SOURCE).getAsJsonObject();
                    appPropsHashtable = new Gson().fromJson(appProps,new TypeToken<Hashtable<String, Object>>() {}.getType());
                }
                if (propertySource.get(Constants.NAME).toString().contains("rule")) {
                    JsonObject ruleProps = propertySource.get(Constants.SOURCE).getAsJsonObject();
                    rulePropsHashtable = new Gson().fromJson(ruleProps,new TypeToken<Hashtable<String, Object>>() {}.getType());
                }
            }
        } else {
           // logger.info(Constants.MISSING_DB_CONFIGURATION);
            throw new RuntimeException(Constants.MISSING_DB_CONFIGURATION);
        }


        configHashtable.putAll(appPropsHashtable);
        configHashtable.putAll(rulePropsHashtable);
        return configHashtable;
    }

}
