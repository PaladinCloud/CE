package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.monitor.ActivityLogAlert;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.ActivityLogVH;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public final class ActivityLogsCollector implements Collector {
    private static final Logger logger = LoggerFactory.getLogger(ActivityLogsCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Insights/activityLogAlerts/%s?api-version=2020-10-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<ActivityLogVH> collect(SubscriptionVH subscription) {
        List<ActivityLogVH> activityLogVHList = new ArrayList<>();

        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(),
                subscription.getSubscriptionId());
        PagedList<ActivityLogAlert> activityLogAlertList;
        try {
            activityLogAlertList = azure.alertRules().activityLogAlerts().list();
        } catch (NullPointerException exception) {
            logger.error("NPE occurred in ActivityLogsCollector by azure.alertRules().activityLogAlerts().list()");
            return activityLogVHList;
        }

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

                logger.info("subscriptionName: {}", subscription.getSubscriptionName());
                JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
                JsonObject activityLogObject = responseObj.getAsJsonObject();

                if (activityLogObject != null) {
                    ActivityLogVH activityLogVH = new ActivityLogVH();
                    activityLogVH.setId(activityLogObject.get("id").getAsString());
                    activityLogVH.setRegion(Util.getRegionValue(subscription,
                            activityLogObject.get("location").getAsString()));
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
                logger.error("Error while fetching activity logs for alert: {}",
                        activityLogAlert.name(), e);
                Util.eCount.getAndIncrement();
            }
        }

        logger.info("Target Type : {}  Total: {} ", "activityLogAlerts", activityLogVHList.size());
        return activityLogVHList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
