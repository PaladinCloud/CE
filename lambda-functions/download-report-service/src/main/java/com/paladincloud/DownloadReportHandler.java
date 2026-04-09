package com.paladincloud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.paladincloud.service.DownloadFileService;
import com.paladincloud.util.PacHttpUtils;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tmobile.pacman.api.commons.Constants.AUTHORIZATION;

public class DownloadReportHandler implements RequestHandler<SQSEvent, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadReportHandler.class);

    private DownloadFileService downloadFileService =  new DownloadFileService();

    //    @Value("${service.dns.name}")
    private String serviceDnsName = "https://saasdev.paladincloud.io";

    @Override
    public String handleRequest(SQSEvent event, Context context) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        for (SQSEvent.SQSMessage msg : event.getRecords()) {

            try {

                JsonObject jsonObject = JsonParser.parseString(msg.getBody()).getAsJsonObject();
                String fileFormat = jsonObject.get("fileFormat").getAsString();
                String token = jsonObject.get("token").getAsString();
                JsonArray filterMapJson = jsonObject.get("filterMap").getAsJsonArray();

                JsonObject filter = jsonObject.get("filter").getAsJsonObject();
//                String request = jsonObject.get("filter").getAsString();


                Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> filterMap = gson.fromJson(filterMapJson, listType);

                Map<String, String> authToken = new HashMap<>();
                authToken.put(AUTHORIZATION, token);

//                String assetGroup = request.getAg();
                JsonArray responseArray = null;
                String serviceName = null;
                String serviceEndpoint = null;

                if (!filterMap.isEmpty()) {
                    for (Map<String, Object> filterMethodName : filterMap) {
                        if (filterMethodName.get("serviceName") != null && filterMethodName.get("serviceEndpoint") != null) {
                            serviceName = filterMethodName.get("serviceName").toString();
                            serviceEndpoint = filterMethodName.get("serviceEndpoint").toString();
                        }
                    }

                    System.out.println("serviceName : " + serviceName);
                    System.out.println("token : " + token);
                    System.out.println("serviceDnsName : " + serviceDnsName);

                    String jsonString = gson.toJson(filter);

                    if (!StringUtils.isEmpty(serviceEndpoint) && !StringUtils.isEmpty(serviceDnsName) && !StringUtils.isEmpty(jsonString)) {

                        String serviceResponse = PacHttpUtils.doHttpsPost(serviceDnsName+serviceEndpoint, jsonString, authToken);
//                        String serviceResponse = "shaik";
                        if(!StringUtil.isNullOrEmpty(serviceResponse)){
                            responseArray = getServiceDetails(serviceResponse);
                            downloadFileService.downloadData(responseArray, fileFormat, serviceName);
//                            return new ResponseEntity<>(HttpStatus.OK);
                            return "";
                        }else{
//                            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                            return "";
                        }
                    } else {
//                        return ResponseUtils.buildFailureResponse(new Exception("Please configure the serviceEndpoint or urlParameters"));
                        return "";
                    }
                } else {
//                    return ResponseUtils.buildFailureResponse(new Exception("Please configure the serviceName and serviceEndpoint"));
                    return "";
                }

            } catch (Exception e) {
                LOGGER.error(e.toString());
                System.out.println(e.toString());
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                return "";
            }
        }
        return "Processed " + event.getRecords().size() + " messages.";
    }

    private JsonArray getServiceDetails(String json) {
        JsonParser jsonParser;
        JsonObject dataJson = null;
        jsonParser = new JsonParser();
        JsonArray resultArray = new JsonArray();
        if (!StringUtils.isEmpty(json)) {
            JsonObject resultJson = (JsonObject) jsonParser.parse(json);
            if (resultJson.get("data").isJsonObject()) {
                dataJson = resultJson.get("data").getAsJsonObject();
                resultArray = dataJson.getAsJsonArray("response");
            }
        }
        return resultArray;

    }


//    Uncomment this code to run this lambda function.
//    public static void main(String[] args) {
//        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
//        String token = "Bearer eyJraWQiOiJuaGhscGxZZ0VpeUpSSmVpVmpqc0JTYSt2cWl2bWRiTnZ6T1BXU01pS000PSIsImFsZyI6IlJTMjU2In0.eyJhdF9oYXNoIjoiWWtXVXBBWjhQRGxROXo5OWx5QW4xUSIsInN1YiI6ImQ0NjgwNGQ4LTAwZjEtNzA0OS1jODcyLTFiOTczMWFjMTMxZiIsImNvZ25pdG86Z3JvdXBzIjpbIlNlY3VyaXR5QWRtaW4iLCJSZWFkT25seSIsIlRlY2huaWNhbEFkbWluIiwiQWNjb3VudE1hbmFnZXIiXSwiZW1haWxfdmVyaWZpZWQiOnRydWUsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC51cy1lYXN0LTEuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0xX0tsbmx3cEN4TiIsImN1c3RvbTpkZWZhdWx0QXNzZXRHcm91cCI6ImF3cyIsImNvZ25pdG86dXNlcm5hbWUiOiJkNDY4MDRkOC0wMGYxLTcwNDktYzg3Mi0xYjk3MzFhYzEzMWYiLCJnaXZlbl9uYW1lIjoiU2hhaWsiLCJvcmlnaW5fanRpIjoiODdjMjFhOTgtZWQwMy00NjdhLWFkYWEtZGUyNmNlZWNmNzAzIiwiYXVkIjoiNzE4YjVpZGowMzlwYnNqc2pqYjgyYms1aDEiLCJldmVudF9pZCI6IjhlYWE5ODQ4LWU0ZTgtNDZmNi1iZWFkLTNiNDBlNDBmMDg0OSIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzc1MTA4MjM0LCJjdXN0b206YWNjZXNzSWQiOiI5OGMyODQ4Mi05YmFlLTQ2YmQtYmQ0ZS01OGZhMTMyZTcyYzAiLCJleHAiOjE3NzUxMTY2NjEsImlhdCI6MTc3NTExMzA2MSwiZmFtaWx5X25hbWUiOiJSYWhhbXRodWxsYSIsImp0aSI6ImM5M2RmMmJmLTgxMTYtNDk5MC05ZmNlLWZkY2M0NjhkNmIxMSIsImVtYWlsIjoic2hhaWsucmFoYW10aHVsbGFAY3liZXJwcm9vZi5jb20ifQ.IzySG3I1bODHJDaWlEESAiU_mfyz31tE2CupA914rzZf-N8VdnzqtYtgYztPPIPVXwmtCRGOCCey9JyOIYwHNjUbjM0ocsMU-L_9ATcG_YXe5Ds0KDX330EVBB7qB1ep0Fh4rEr0Bm0MRlT_8rEoEr7ruwUboOTDtIEtDLq2Bq1kNOK2TaXt9C8j6BrQydSSD1ZGFCNzniDxpTjSYSS4YeDBPzn6nBfRPJUhASeUFMYCIvV9d-7Z9pAB-wlFGeaCkqB9XrQDnQwsym_6Gla81vVBIpRKfjDxtA-4qMgFgfLyUE7BoLvkSGxanUX2Q3yPhVGcjnrXgyhQycHrePnKeA";
//        message.setBody(String.format("{\"_docId\": 123, \"token\": \"%s\", \"filterMap\": [{\"serviceName\":\"Violations\",\"serviceEndpoint\":\"/api/compliance/v1/issues\"}], \"fileFormat\": \"csv\", \"filter\": {\"ag\": \"all-sources\", \"filter\": {\"issueStatus.keyword\": [\"open\"], \"domain\": \"Infra & Platforms\"}, \"sortFilter\": {\"fieldName\": \"issueStatus.keyword\", \"fieldType\": \"string\", \"order\": \"desc\", \"sortOrder\": null}, \"from\": 0, \"searchtext\": \"\", \"size\": 5803}}", token));
//
//        SQSEvent event = new SQSEvent();
//        event.setRecords(Collections.singletonList(message));
//
//        DownloadReportHandler handler = new DownloadReportHandler();
//        String result = handler.handleRequest(event, null);
//    }
}
