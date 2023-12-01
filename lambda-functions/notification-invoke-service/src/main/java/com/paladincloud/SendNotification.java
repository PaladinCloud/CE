package com.paladincloud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class SendNotification implements RequestHandler<Map<String,Object>, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendNotification.class);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     *Below method places all incoming notification requests into topic which are consumed
     * by subsequent lambda functions.
     *
     */
    @Override
    public String handleRequest(Map<String,Object> event, Context context)
    {
        String response = new String("hello paladin cloud");
        LOGGER.info("EVENT: " + gson.toJson(event));
        AmazonSNS client = AmazonSNSClientBuilder.standard().build();
        try {
            String snsTopicArn = System.getenv("SNS_TOPIC_ARN");
            String payloadString = (String)event.get("body");
            if(payloadString==null || payloadString.length()==0){
                String jsonStr = gson.toJson(event);
                LOGGER.info("jsonStr -- "+jsonStr);
                JsonParser jsonParser = new JsonParser();
                JsonObject policyParamsJson = (JsonObject) jsonParser.parse(jsonStr);
                JsonElement bodyJson = policyParamsJson.getAsJsonArray("Records").get(0);
                LOGGER.info("bodyJson--"+bodyJson.toString());
                payloadString = bodyJson.getAsJsonObject().get("Sns").getAsJsonObject().get("Message").getAsString();
            }

            Object payloadObj = gson.fromJson(payloadString,Object.class);
            if(payloadObj instanceof List){
                List<Object> notificationRequestList = gson.fromJson(payloadString,List.class);
                notificationRequestList.stream().forEach(obj -> {
                    String notificationRequestStr = gson.toJson(obj);
                    LOGGER.info("notificationsrequeststr for list---"+notificationRequestStr);
                    PublishRequest request = new PublishRequest(snsTopicArn,notificationRequestStr);
                    PublishResult result = client.publish(request);
                    LOGGER.info("message sent with id "+result.getMessageId());
                });
            }
            else{

                LOGGER.info("notificationsrequeststr for single event---"+payloadString);
                PublishRequest request = new PublishRequest(snsTopicArn,payloadString);
                PublishResult result = client.publish(request);
                LOGGER.info("message sent with id "+result.getMessageId());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }
    }
