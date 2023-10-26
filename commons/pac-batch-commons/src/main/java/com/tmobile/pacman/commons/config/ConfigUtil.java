/*******************************************************************************
 * Copyright 2022 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.pacman.commons.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {

    private static Logger log = LoggerFactory.getLogger(ConfigUtil.class);
    
    private static String configUrl = System.getenv("CONFIG_URL")!=null?System.getenv("CONFIG_URL"):System.getenv("CONFIG_SERVICE_URL");
    private static final String NAME = "name";

    private static final String SOURCE = "source";

    private static final String APPLICATION = "application";

    private static final String BATCH = "batch";

    public static void setConfigProperties(String base64Creds, String inventory) throws Exception {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.putAll(fetchConfigProperties(base64Creds,inventory));
        System.setProperties(properties);
    }

    @SuppressWarnings("unchecked")
    public static Map<String,String> fetchConfigProperties(String base64Creds, String inventory) throws Exception {

        Map<String,String> properties = new HashMap<>();


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String,String> appProps = new HashMap<>();
            Map<String,String> batchProps = new HashMap<>();
            Map<String,String> invProps = new HashMap<>();
            Map<String,Object> response = objectMapper.readValue(Util.httpGetMethodWithHeaders(configUrl, Util.getHeader(base64Creds)), new TypeReference<Map<String,Object>>(){});
            List<Map<String,Object>> propertySources = (List<Map<String,Object>>)response.get("propertySources");
            for(Map<String,Object> propertySource : propertySources) {
                if(propertySource.get(NAME).toString().contains(APPLICATION)) {
                    appProps.putAll((Map<String,String>)propertySource.get(SOURCE));
                }
                if(propertySource.get(NAME).toString().contains(BATCH)) {
                    batchProps.putAll((Map<String,String>)propertySource.get(SOURCE));
                }
                if(propertySource.get(NAME).toString().contains(inventory)) {
                    invProps.putAll((Map<String,String>)propertySource.get(SOURCE));
                }
                properties.putAll(appProps);
                properties.putAll(batchProps);
                properties.putAll(invProps);
            }
        } catch (Exception e) {
            log.error("Error in fetchConfigProperties",e);
            throw e;
        }
        if(properties.isEmpty()){
            throw new Exception("No config properties fetched from "+configUrl);
        }

        log.info("Config are feteched from {}",configUrl);
        properties.forEach((k,v)-> log.debug("   {} : {} ",k,v));
        return properties;
    }
}
