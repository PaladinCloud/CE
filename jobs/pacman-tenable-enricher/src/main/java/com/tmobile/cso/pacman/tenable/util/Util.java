package com.tmobile.cso.pacman.tenable.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


/**
 * The Class Util.
 */
public class Util {
    
    private static Logger log = LoggerFactory.getLogger(Util.class);

    /**
     * Base 64 decode.
     *
     * @param encodedStr the encoded str
     * @return the string
     */
    public static String base64Decode(String encodedStr) {
        try {
            return new String(BaseEncoding.base64().decode(encodedStr), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Error in base64Decode",e);
            return "";
        }
    }

    public static Map<String,String> getJsonData(String jsonString){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map= Collections.emptyMap();
        try {
            // convert JSON string to Map
            map = mapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            log.error("Error in parsing json data",e);
        }
        return map;
    }
    
    /**
     * Gets the header.
     *
     * @param base64Creds the base 64 creds
     * @return the header
     */
    public static Map<String,Object> getHeader(String base64Creds){
        Map<String,Object> authToken = new HashMap<>();
        authToken.put("Content-Type", ContentType.APPLICATION_JSON.toString());
        authToken.put("Authorization", "Basic "+base64Creds);
        return authToken;
    }
    public static Object getJsonAttribute(String jsonString, String attributeName){
        Gson gson = new Gson();
        Map map = gson.fromJson(jsonString, Map.class);
        return map.get(attributeName);
    }


    public static Gson getJsonBuilder(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            @Override
            public Date deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
                throws JsonParseException {
                try {
                    return df.parse(json.getAsString());
                } catch (ParseException e) {
                    log.error("Error in getting Json builder ", e);
                }
                catch (Exception e) {
                    log.error("Error in getting Json builder ", e);
                }
                return null;
            }
        });
        return gsonBuilder.setPrettyPrinting().create();
    }
}
