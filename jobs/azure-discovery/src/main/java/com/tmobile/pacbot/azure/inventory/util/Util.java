package com.tmobile.pacbot.azure.inventory.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static Map<String,String> getJson(String jsonString){
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> dataMap= Collections.emptyMap();
        try {
            // convert JSON string to Map
            dataMap = objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            LOGGER.error("Error in parsing json data",e);
        }
        return dataMap;
    }
}
