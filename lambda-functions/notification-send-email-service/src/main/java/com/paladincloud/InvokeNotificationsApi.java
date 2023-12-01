package com.paladincloud;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paladincloud.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class InvokeNotificationsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeNotificationsApi.class);

    public String handleRequest(Map<String,Object> event, Context context)
    {
        Gson gsonObj = new Gson();

        String response = new String("hello paladin cloud");
        LOGGER.info("inside handleRequest "+event);
        String jsonStr = gsonObj.toJson(event);
        LOGGER.info("jsonStr -- "+jsonStr);
        JsonParser jsonParser = new JsonParser();
        JsonObject policyParamsJson = (JsonObject) jsonParser.parse(jsonStr);

        JsonElement bodyJson = policyParamsJson.getAsJsonArray("Records").get(0);
        LOGGER.info("bodyJson--"+bodyJson.toString());
        String payload = bodyJson.getAsJsonObject().get("Sns").getAsJsonObject().get("Message").getAsString();

        LOGGER.info("payload for invoking notifications "+payload);

        try {
            String invokeNotificationUrl = System.getenv("INVOKE_NOTIFICATION_URL");
            String token = HttpUtil.getToken();
            LOGGER.info(" token -"+token);
            String notificationSettingsJson = HttpUtil.post(invokeNotificationUrl,payload,token,"Bearer");
            LOGGER.info("notification settings response -"+notificationSettingsJson);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}