package com.tmobile.cso.pacman.aqua.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tmobile.cso.pacman.aqua.Constants;


/**
 * The Class Util.
 */
public class Util {
    
    private static Logger log = LoggerFactory.getLogger(Util.class);

    private static List<Map<String,String>> errorList = new ArrayList<>();


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


    public static Gson getJsonBuilder() throws ParseException{
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
