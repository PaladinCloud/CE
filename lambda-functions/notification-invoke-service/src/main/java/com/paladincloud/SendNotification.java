package com.paladincloud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendNotification implements RequestHandler<Map<String, Object>, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendNotification.class);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Below method places all incoming notification requests into topic which are consumed by
     * subsequent lambda functions.
     */
    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        String response = "hello paladin cloud";
        LOGGER.info("EVENT: {}", gson.toJson(event));
        AmazonSNS client = AmazonSNSClientBuilder.standard().build();
        try {
            String snsTopicArn = System.getenv("SNS_TOPIC_ARN");
            String payloadString = (String) event.get("body");
            if (payloadString == null || payloadString.isEmpty()) {
                String jsonStr = gson.toJson(event);
                LOGGER.info("jsonStr -- {}", jsonStr);
                JsonObject policyParamsJson = (JsonObject) JsonParser.parseString(jsonStr);
                JsonArray records = policyParamsJson.getAsJsonArray("Records");

                records.forEach(body -> {
                    String payload = body.getAsJsonObject()
                        .get("Sns").getAsJsonObject()
                        .get("Message").getAsString();
                    sendPayload(payload, client, snsTopicArn);
                });
            } else {
                sendPayload(payloadString, client, snsTopicArn);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private void sendPayload(String payloadString, AmazonSNS client, String snsTopicArn) {
        Object payloadObj = gson.fromJson(payloadString, Object.class);
        if (payloadObj instanceof List) {
            List<Object> notificationRequestList = gson.fromJson(payloadString, List.class);
            notificationRequestList.stream().forEach(obj -> {
                try {
                    String notificationRequestStr = gson.toJson(obj);
                    LOGGER.info("notificationsrequeststr for list---{}", notificationRequestStr);
                    PublishRequest request = new PublishRequest(snsTopicArn, notificationRequestStr);
                    PublishResult result = client.publish(request);
                    LOGGER.info("message sent with id {}", result.getMessageId());
                } catch (Exception ex) {
                    LOGGER.error("Failed sending message: {}", obj, ex);
                }
            });
        } else {
            try {
                LOGGER.info("notificationsrequeststr for single event---{}", payloadString);
                PublishRequest request = new PublishRequest(snsTopicArn, payloadString);
                PublishResult result = client.publish(request);
                LOGGER.info("message sent with id {}", result.getMessageId());
            } catch (Exception ex) {
                LOGGER.error("Failed sending message: {}", payloadString, ex);
            }
        }
    }
}
