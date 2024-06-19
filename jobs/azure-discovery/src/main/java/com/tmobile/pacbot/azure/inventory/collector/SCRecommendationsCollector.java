package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.RecommendationVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SCRecommendationsCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(SCRecommendationsCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.Security/tasks?api-version=2015-06-01-preview";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<RecommendationVH> collect(SubscriptionVH subscription) {
        List<RecommendationVH> recommendations = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String url = String.format(apiUrlTemplate, subscription.getSubscriptionId());

        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            recommendations = filterRecommendationInfo(response, subscription);
        } catch (Exception exception) {
            String errorMessage = String.format("Error occurred while collecting SCRecommendations for subscriptionId: %s, subscriptionName: %s", subscription.getSubscriptionId(), subscription.getSubscriptionName());
            log.error(errorMessage, exception);
            Util.eCount.getAndIncrement();
            log.debug("Current error count after exception occurred in SCRecommendations collector: {}", Util.eCount.get());

        }

        log.info("Target Type : {}  Total: {} ", "Security Center", recommendations.size());
        return recommendations;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }

    private List<RecommendationVH> filterRecommendationInfo(String response, SubscriptionVH subscription) {
        List<RecommendationVH> recommendations = new ArrayList<>();
        JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
        JsonArray recommendationObjects = responseObj.getAsJsonArray("value");

        for (JsonElement recElmnt : recommendationObjects) {
            JsonObject recommendObject = recElmnt.getAsJsonObject();
            JsonObject properties = recommendObject.getAsJsonObject("properties");
            String id = recommendObject.get("id").getAsString();
            String region = id.substring(id.indexOf("locations/") + "locations/".length() + 1, id.indexOf("/tasks"));
            if ("Active".equals(properties.get("state").getAsString())) {
                JsonObject secTaskParameters = properties.getAsJsonObject("securityTaskParameters");
                //String baseLineName = secTaskParameters.get("baselineName")!=null?secTaskParameters.get("baselineName").getAsString():null;
                String policyName = secTaskParameters.get("policyName") != null ? secTaskParameters.get("policyName").getAsString() : null;
                //String name = secTaskParameters.get("name")!=null?secTaskParameters.get("name").getAsString():null;
                String resourceType = secTaskParameters.get("resourceType") != null ? secTaskParameters.get("resourceType").getAsString() : "";

                if (policyName != null && "VirtualMachine".equals(resourceType)) {
                    Map<String, Object> recommendationMap = new Gson().fromJson(secTaskParameters, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    Object resourceId = recommendationMap.get("resourceId");
                    String recommendationName = String.valueOf(recommendationMap.get("name") != null ? recommendationMap.get("name") : "");
                    if (resourceId != null) {
                        RecommendationVH recommendation = new RecommendationVH();
                        recommendation.setSubscription(subscription.getSubscriptionId());
                        recommendation.setSubscriptionName(subscription.getSubscriptionName());
                        recommendationMap.put("resourceId", Util.removeFirstSlash(resourceId.toString()));
                        recommendationMap.put("_resourceIdLower", Util.removeFirstSlash(resourceId.toString()).toLowerCase());
                        recommendation.setId(id);
                        recommendation.setName(recommendationName);
                        recommendation.setRegion(Util.getRegionValue(subscription, region));
                        recommendation.setRecommendation(recommendationMap);
                        recommendation.setResourceGroupName(Util.getResourceGroupNameFromId(recommendation.getId()));
                        recommendations.add(recommendation);
                    }
                }
            }
        }

        return recommendations;
    }
}
