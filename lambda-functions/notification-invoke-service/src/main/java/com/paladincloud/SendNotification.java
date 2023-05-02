package com.paladincloud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.gson.*;

import java.util.List;
import java.util.Map;


public class SendNotification implements RequestHandler<Map<String,Object>, String> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     *Below method places all incoming notification requests into topic which are consumed
     * by subsequent lambda functions.
     *
     */
    @Override
    public String handleRequest(Map<String,Object> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        String response = new String("hello paladin cloud");
        logger.log("EVENT: " + gson.toJson(event));
        AmazonSNS client = AmazonSNSClientBuilder.standard().build();
        try {
            String snsTopicArn = System.getenv("SNS_TOPIC_ARN");
            String payloadString = (String)event.get("body");
            if(payloadString==null || payloadString.length()==0){
                String jsonStr = gson.toJson(event);
                System.out.println("jsonStr -- "+jsonStr);
                JsonParser jsonParser = new JsonParser();
                JsonObject policyParamsJson = (JsonObject) jsonParser.parse(jsonStr);
                JsonElement bodyJson = policyParamsJson.getAsJsonArray("Records").get(0);
                System.out.println("bodyJson--"+bodyJson.toString());
                payloadString = bodyJson.getAsJsonObject().get("Sns").getAsJsonObject().get("Message").getAsString();
            }

            Object payloadObj = gson.fromJson(payloadString,Object.class);
            if(payloadObj instanceof List){
                List<Object> notificationRequestList = gson.fromJson(payloadString,List.class);
                notificationRequestList.stream().forEach(obj -> {
                    String notificationRequestStr = gson.toJson(obj);
                    System.out.println("notificationsrequeststr for list---"+notificationRequestStr);
                    PublishRequest request = new PublishRequest(snsTopicArn,notificationRequestStr);
                    PublishResult result = client.publish(request);
                    logger.log("message sent with id "+result.getMessageId());
                });
            }
            else{
                System.out.println("notificationsrequeststr for single event---"+payloadObj.toString());
                PublishRequest request = new PublishRequest(snsTopicArn,payloadObj.toString());
                PublishResult result = client.publish(request);
                logger.log("message sent with id "+result.getMessageId());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }
    }
