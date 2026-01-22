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
    private static final String NOTIFICATION_INVOKE_SVC = "notification-invoke-svc";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final int maxDescriptionLength = 1000;

    /**
     * Below method places all incoming notification requests into topic which are consumed by
     * subsequent lambda functions.
     */
    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        String response = "hello paladin cloud";
        PaladinMetrics.initialize("component", "invoke-svc");
        AmazonSNS client = AmazonSNSClientBuilder.standard().build();
        try {
            PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-invoke");
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
            PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-invoke-success");
        } catch (Throwable e) {
            LOGGER.error("Error sending to SNS", e);
            PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-invoke-error");
            throw new RuntimeException(e);
        } finally {
            PaladinMetrics.close();
        }

        return response;
    }

    private void sendPayload(String payloadString, AmazonSNS client, String snsTopicArn) {
        Object payloadObj = gson.fromJson(payloadString, Object.class);
        if (payloadObj instanceof List) {
            List<Object> notificationRequestList = gson.fromJson(payloadString, List.class);
            notificationRequestList.stream().forEach(obj -> {
                try {
                    PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-send");
                    String notificationRequestStr = trimDescription(gson.toJson(obj));
                    LOGGER.info("notificationsrequeststr for list---{}", notificationRequestStr);
                    PublishRequest request = new PublishRequest(snsTopicArn, notificationRequestStr);
                    PublishResult result = client.publish(request);
                    PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-send-success");
                    LOGGER.info("message sent with id {}", result.getMessageId());
                } catch (Exception ex) {
                    PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-send-error");
                    LOGGER.error("Failed sending message: length={}; {}", obj.toString().length(), obj, ex);
                }
            });
        } else {
            try {
                PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-send");
                payloadString = trimDescription(payloadString);
                LOGGER.info("notificationsrequeststr for single event---{}", payloadString);
                PublishRequest request = new PublishRequest(snsTopicArn, payloadString);
                PublishResult result = client.publish(request);
                PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-send-success");
                LOGGER.info("message sent with id {}", result.getMessageId());
            } catch (Exception ex) {
                PaladinMetrics.incrementCount(NOTIFICATION_INVOKE_SVC + "-send-error");
                LOGGER.error("Failed sending message: length={}; {}", payloadString.length(), payloadString, ex);
            }
        }
    }

    private String trimDescription(String jsonStr) {
        JsonObject jo = (JsonObject) JsonParser.parseString(jsonStr);
        JsonObject payloadObj = (JsonObject) jo.get("payload");
        if (payloadObj != null) {
            JsonElement descObj = payloadObj.get("description");
            if (descObj != null && descObj.getAsJsonPrimitive().isString()) {
                String description = descObj.getAsString();
                if (description.length() > maxDescriptionLength) {
                    LOGGER.warn("Truncating description length={}", description.length());
                    payloadObj.addProperty("description", description.substring(0, maxDescriptionLength));
                    return jo.toString();
                }
            }
        }
        return jsonStr;
    }
}
