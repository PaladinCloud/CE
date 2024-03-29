/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    private ConfigUtil() {
        throw new IllegalStateException("ConfigUtil is a utility class");
    }

    public static void setConfigProperties(String configCreds) throws Exception {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.putAll(fetchConfigProperties(configCreds));
        System.setProperties(properties);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> fetchConfigProperties(String configCreds) throws Exception {
        Map<String, String> properties = new HashMap<>();

        String configUrl = System.getenv("CONFIG_URL");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, String> appProps = new HashMap<>();
            Map<String, String> batchProps = new HashMap<>();
            Map<String, String> invProps = new HashMap<>();
            Map<String, String> jobSchedulerProps = new HashMap<>();
            Map<String, Object> response = objectMapper.readValue(HttpUtil.httpGetMethodWithHeaders(configUrl, Util.getHeader(configCreds)), new TypeReference<Map<String, Object>>() {
            });
            List<Map<String, Object>> propertySources = (List<Map<String, Object>>) response.get("propertySources");
            for (Map<String, Object> propertySource : propertySources) {
                if (propertySource.get(Constants.NAME).toString().contains("application")) {
                    appProps.putAll((Map<String, String>) propertySource.get(Constants.SOURCE));
                }

                if (propertySource.get(Constants.NAME).toString().contains("batch")) {
                    batchProps.putAll((Map<String, String>) propertySource.get(Constants.SOURCE));
                }

                if (propertySource.get(Constants.NAME).toString().contains("data-shipper")) {
                    invProps.putAll((Map<String, String>) propertySource.get(Constants.SOURCE));
                }

                if (propertySource.get(Constants.NAME).toString().contains("job-scheduler")) {
                    jobSchedulerProps.putAll((Map<String, String>) propertySource.get(Constants.SOURCE));
                }

                properties.putAll(appProps);
                properties.putAll(batchProps);
                properties.putAll(invProps);
                properties.putAll(jobSchedulerProps);
            }
        } catch (Exception e) {
            LOGGER.error("Error in fetchConfigProperties", e);
            throw e;
        }
        if (properties.isEmpty()) {
            throw new Exception("No config properties fetched from " + configUrl);
        }

        LOGGER.info("Config are fetched from {}", configUrl);
        return properties;
    }
}
