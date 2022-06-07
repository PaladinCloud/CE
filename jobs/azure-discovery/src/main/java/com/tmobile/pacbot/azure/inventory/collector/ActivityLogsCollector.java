
package com.tmobile.pacbot.azure.inventory.collector;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.PagedList;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.ActivityLogVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.monitor.ActivityLogAlert;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

@Component
public final class ActivityLogsCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;
    private String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Insights/activityLogAlerts/%s?api-version=2020-10-01";
    private static Logger logger = LoggerFactory.getLogger(ActivityLogsCollector.class);

    public List<ActivityLogVH> fetchActivityLogAlertDetails(SubscriptionVH subscription) {
        List<ActivityLogVH> activityLogVHList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<ActivityLogAlert> activityLogAlertList = azure.alertRules().activityLogAlerts().list();

        for (ActivityLogAlert activityLogAlert : activityLogAlertList) {
            try {
                String url = String.format(apiUrlTemplate,
                        URLEncoder.encode(subscription.getSubscriptionId(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(activityLogAlert.resourceGroupName(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(activityLogAlert.name(), java.nio.charset.StandardCharsets.UTF_8.toString()));
                String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
                JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
                JsonArray activityLogObjects = responseObj.getAsJsonArray("value");

                if (activityLogObjects != null) {
                    for (JsonElement activityLogElement : activityLogObjects) {
                        ActivityLogVH activityLogVH = new ActivityLogVH();
                        JsonObject activityLogObject = activityLogElement.getAsJsonObject();
                        JsonObject properties = activityLogObject.getAsJsonObject("properties");

                        JsonObject condition = properties.getAsJsonObject("condition");

                        if (condition != null) {
                            HashMap<String, Object> conditionMap = new Gson().fromJson(condition.toString(),
                                    HashMap.class);
                            activityLogVH.setAllof((List<Map<String, Object>>) conditionMap.get("allOf"));

                        }

                        activityLogVHList.add(activityLogVH);
                    }
                }

            } catch (Exception e) {

            }

        }
        return activityLogVHList;
    }

}
