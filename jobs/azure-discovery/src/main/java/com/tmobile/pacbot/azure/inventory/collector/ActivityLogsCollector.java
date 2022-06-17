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
import com.tmobile.pacbot.azure.inventory.vo.ActivityLogAlertRuleVH;
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

        public List<ActivityLogAlertRuleVH> fetchActivityLogAlertDetails(SubscriptionVH subscription) {
                List<ActivityLogVH> activityLogVHList = new ArrayList<>();
                List<ActivityLogAlertRuleVH> activityLogAlertsRuleVHList = new ArrayList<>();
                ActivityLogAlertRuleVH activityLogAlertsRuleVH = new ActivityLogAlertRuleVH();
                String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
                Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(),
                                subscription.getSubscriptionId());
                PagedList<ActivityLogAlert> activityLogAlertList = azure.alertRules().activityLogAlerts().list();
                String resourceGroupName = activityLogAlertList.size() > 0
                                ? activityLogAlertList.get(0).resourceGroupName()
                                : "";
                activityLogAlertsRuleVH.setId("subscriptions/" + subscription.getSubscriptionId() + "/resourceGroups/"
                                + resourceGroupName + "/providers/microsoft.insights/activityLogAlerts/");
                activityLogAlertsRuleVH.setSubscription(subscription.getSubscriptionId());
                activityLogAlertsRuleVH.setSubscriptionName(subscription.getSubscriptionName());
                activityLogAlertsRuleVH.setResourceGroupName(resourceGroupName);

                logger.info("activityLogAlertList size : {}  ", activityLogAlertList.size());
                for (ActivityLogAlert activityLogAlert : activityLogAlertList) {

                        try {
                                String url = String.format(apiUrlTemplate,
                                                URLEncoder.encode(subscription.getSubscriptionId(),
                                                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                                                URLEncoder.encode(activityLogAlert.resourceGroupName(),
                                                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                                                URLEncoder.encode(activityLogAlert.name(),
                                                                java.nio.charset.StandardCharsets.UTF_8.toString()));
                                String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
                                logger.info("response form API: {} for log alert name: {}",
                                                response,
                                                activityLogAlert.name().isEmpty() ? activityLogAlert.name() : "");
                                logger.info("subscriptionName: {}", subscription.getSubscriptionName());
                                JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
                                JsonObject activityLogObject = responseObj.getAsJsonObject();
                                logger.info("activityLogObject: {}", activityLogObject);

                                if (activityLogObject != null) {
                                        ActivityLogVH activityLogVH = new ActivityLogVH();

                                        activityLogVH.setId(activityLogObject.get("id").getAsString());
                                        if (activityLogAlertsRuleVH.getRegion() == null) {
                                                activityLogAlertsRuleVH.setRegion(
                                                                activityLogObject.get("location").getAsString());
                                        }
                                        activityLogVH.setRegion(
                                                        activityLogObject.get("location").getAsString());
                                        activityLogVH.setSubscription(subscription.getSubscriptionId());
                                        activityLogVH.setSubscriptionName(subscription.getSubscriptionName());
                                        activityLogVH.setResourceGroupName(
                                                        activityLogAlert.resourceGroupName());
                                        JsonObject properties = activityLogObject.getAsJsonObject("properties");
                                        if (properties != null) {
                                                HashMap<String, Object> propertiesMap = new Gson().fromJson(
                                                                properties.toString(),
                                                                HashMap.class);
                                                activityLogVH.setProperties(propertiesMap);
                                        }

                                        activityLogVHList.add(activityLogVH);

                                }

                        } catch (Exception e) {

                        }

                }
                activityLogAlertsRuleVH.setActivityLogAlerts(activityLogVHList);
                activityLogAlertsRuleVHList.add(activityLogAlertsRuleVH);

                logger.info("Target Type : {}  Total: {} ", "activityLogAlerts", activityLogVHList.size(),
                                activityLogAlertsRuleVH);

                return activityLogAlertsRuleVHList;
        }

}
