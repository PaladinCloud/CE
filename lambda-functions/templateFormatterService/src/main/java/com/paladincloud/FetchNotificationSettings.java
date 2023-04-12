package com.paladincloud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.common.base.Strings;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;


import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.paladincloud.Constants.*;

public class FetchNotificationSettings {
    public String handleRequest(Map<String,Object> event, Context context)
    {
        Gson gsonObj = new Gson();
        LambdaLogger logger = context.getLogger();

        String response = new String("hello paladin cloud");
        logger.log("inside handleRequest "+event);
        String jsonStr = gsonObj.toJson(event);
        System.out.println("jsonStr -- "+jsonStr);
        JsonParser jsonParser = new JsonParser();
        JsonObject policyParamsJson = (JsonObject) jsonParser.parse(jsonStr);

        JsonElement bodyJson = policyParamsJson.getAsJsonArray("Records").get(0);
        System.out.println("bodyJson--"+bodyJson.toString());
        String requestObjectStr = bodyJson.getAsJsonObject().get("Sns").getAsJsonObject().get("Message").getAsString();
        Map requestMap = gsonObj.fromJson(requestObjectStr, Map.class);
        String notificationType = ((String)requestMap.get("eventCategory")).toLowerCase();
        String subject = (String)requestMap.get("subject");

        Map<String,Object> messageContentMap = (Map<String,Object>)requestMap.get("payload");
        String action = ((String)messageContentMap.get("action"));
        String exemptionType = (String)messageContentMap.get("type");
        System.out.println("notificationtype - "+notificationType+"map--"+requestMap);


        try {

            Map<String,String> configDetailsMap = HttpUtil.getConfigDetailsForChannels();
            String notificationSettingsJson="";
            if(messageContentMap.containsKey("configDetails")){
                Map<String, Object> configMap = (Map<String,Object>)messageContentMap.get("configDetails");
                notificationSettingsJson = gsonObj.toJson(configMap);
            }
            else{

                /*
            Sample response from notification_settings_url will be like the below
      {
        "settings":{
                "exemptions":{
                        "email":{
                            "toAddress":["ADMIN","MANAGER"],
                            "isOn":true
                        },
                        "slack":{
                            "toAddress":["ADMIN","MANAGER"],
                            "isOn":false
                        },
                        "jira":{
                            "toAddress":["ADMIN","MANAGER"],
                            "isOn":false
                        }
                },
                "violations":{
                        "email":{
                            "toAddress":["ADMIN","MANAGER"],
            	            "isOn":false
                        },
                        "slack":{
                             "toAddress":["ADMIN","MANAGER"],
                            "isOn":false
                        },
                        "jira":{
                            "toAddress":["ADMIN","MANAGER"],
                            "isOn":false
                        }
                }
          },
            "destinations":{
                "email":"queuename:arn",
                "slack":"queuename:arn"
            }
        }
         */
                String accessToken = HttpUtil.getToken(configDetailsMap.get(apiauthinfo));
                logger.log(" token -"+accessToken);
                String notificationSettingsUrl = System.getenv("NOTIFICATION_SETTINGS_URL");
                notificationSettingsJson = HttpUtil.get(notificationSettingsUrl,accessToken);
            }
            logger.log("notification settings response -"+notificationSettingsJson);
            
            JsonObject notifySettingsJsonObject = (JsonObject) jsonParser.parse(notificationSettingsJson);
            Optional reqNotificationTypeOptional = Optional.ofNullable(notifySettingsJsonObject).map(obj -> obj.getAsJsonObject("data")).map(obj -> obj.getAsJsonObject("settings")).map(obj -> obj.getAsJsonObject(notificationType));
            if(reqNotificationTypeOptional.isPresent()) {
                JsonObject notifyTypeJsonObj = (JsonObject) reqNotificationTypeOptional.get();
                Iterator channelIterator = notifyTypeJsonObj.keySet().iterator();

                //iterate the channels for the notificationType of request
                while (channelIterator.hasNext()) {
                    String channel = (String) channelIterator.next();
                    Integer sendNotification =  notifyTypeJsonObj.getAsJsonObject(channel).get("isOn").getAsInt();
                    JsonArray toEmailIdJsonArray =  notifyTypeJsonObj.getAsJsonObject(channel).getAsJsonArray("toAddress");
                    List<String> toEmailIdList = toEmailIdJsonArray.asList().stream().filter(obj -> !Strings.isNullOrEmpty(obj.getAsString())).map(obj -> obj.getAsString()).collect(Collectors.toList());
                    logger.log("sendNotification - "+sendNotification+" toEmailIdList - "+toEmailIdList);

                    //if sendNotification is 1, email will be published. Else, no.
                    if(Integer.valueOf(1).equals(sendNotification) && !toEmailIdList.isEmpty()){
                        AmazonSNS client = AmazonSNSClientBuilder.standard().build();
                        ClassLoader classLoader = getClass().getClassLoader();
                        logger.log("key - "+channel+" action- "+action+" notificationtype- "+notificationType+" exemptionType- "+exemptionType);
                        File file = new File(classLoader.getResource(CommonUtils.getTemplateName(channel, action, notificationType, exemptionType)).getFile());
                        String messageContent = buildPlainTextMail(FileUtils.readFileToString(file, "UTF-8"), messageContentMap);

                        String notificationDetailsStr = gsonObj.toJson(getMsgDetailsMap(messageContent,toEmailIdList, subject));
                        logger.log("notification message for channel '"+channel+"' is - "+messageContent);
                        if(configDetailsMap.containsKey(channel)){
                            PublishRequest request = new PublishRequest(configDetailsMap.get(channel),notificationDetailsStr);
                            PublishResult result = client.publish(request);
                            logger.log("Notification message sent for "+channel+" with id "+result.getMessageId());
                        }
                        else{
                            logger.log("Topic ARN for "+channel+ " notification is not configured.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private String buildPlainTextMail(String mailBody, final Map<String, Object> details) {
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            if(entry.getValue()!=null &&  !"additionalInfo".equalsIgnoreCase(entry.getKey()) && mailBody.contains("${".concat(entry.getKey()).concat("}"))){
                mailBody = mailBody.replace("${".concat(entry.getKey()).concat("}"), entry.getValue().toString());
            }
        }
        return mailBody;
    }

    /**
     *
     * @param messageContent
     * @param toEmailIdList
     * @param subject
     * @return
     */
    private Map<String, Object> getMsgDetailsMap(String messageContent,List<String> toEmailIdList, String subject){
        Map<String,Object> msgDetailsMap = new HashMap<>();
        msgDetailsMap.put("mailBodyAsString",messageContent);
        msgDetailsMap.put("from",fromEmail);
        msgDetailsMap.put("to",toEmailIdList);
        msgDetailsMap.put("subject",subject);
        msgDetailsMap.put("placeholderValues", Collections.EMPTY_MAP);
        return msgDetailsMap;
    }
}