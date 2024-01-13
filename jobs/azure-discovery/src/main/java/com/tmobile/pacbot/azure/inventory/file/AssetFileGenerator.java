package com.tmobile.pacbot.azure.inventory.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.management.Azure;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.collector.*;
import com.tmobile.pacbot.azure.inventory.vo.ResourceGroupVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.database.RDSDBManager;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tmobile.pacbot.azure.inventory.util.Constants.ERROR_PREFIX;
import static com.tmobile.pacbot.azure.inventory.util.ErrorManageUtil.triggerNotificationforPermissionDenied;
import static com.tmobile.pacbot.azure.inventory.util.TargetTypesConstants.*;

@Component
public class AssetFileGenerator {

    private static final Logger log = LoggerFactory.getLogger(AssetFileGenerator.class);

    @Autowired
    RDSDBManager rdsdbManager;

    @Value("${targetTypes:}")
    private String targetTypes;

    @Autowired
    private AzureCredentialProvider azureCredentialProvider;

    @Autowired
    private ResourceGroupInventoryCollector resourceGroupInventoryCollector;

    @Autowired
    private AssetDataFactory assetDataFactory;

    public void generateFiles(List<SubscriptionVH> subscriptions, String filePath) {

        try {
            FileManager.initialise(filePath);
        } catch (Exception e1) {
            log.error(ERROR_PREFIX + "Failed to create file in S3 in path + " + filePath, e1);
            System.exit(1);
        }

        List<String> connectedSubscriptions = new ArrayList<>();
        for (SubscriptionVH subscription : subscriptions) {
            log.info("Started Discovery for sub {}", subscription);
            try {
                String accessToken = azureCredentialProvider.getAuthToken(subscription.getTenant());
                Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(),
                        subscription.getSubscriptionId());
                azureCredentialProvider.putClient(subscription.getTenant(), subscription.getSubscriptionId(), azure);
                azureCredentialProvider.putToken(subscription.getTenant(), accessToken);
                rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='configured' WHERE tenant=? AND subscription=?", Arrays.asList(subscription.getTenant(), subscription.getSubscriptionId()));
                log.info("updating account status of azure subscription- {} to online.", subscription.getSubscriptionId());
                connectedSubscriptions.add(subscription.getSubscriptionId());
            } catch (Exception e) {
                log.error("Error authenticating for {}", subscription, e);
                rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline' WHERE tenant=? AND subscription=?", Arrays.asList(subscription.getTenant(), subscription.getSubscriptionId()));
                log.error("updating account status of azure subscription- {} to offline.", subscription.getSubscriptionId());
                continue;
            }
            subscription.setRegions(getRegionsFromAzure(subscription));
            List<ResourceGroupVH> resourceGroupList = new ArrayList<>();
            try {
                resourceGroupList = resourceGroupInventoryCollector.collect(subscription);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Map<String, String>> tagMap = resourceGroupList.stream()
                    .collect(Collectors.toMap(x -> x.getResourceGroupName().toLowerCase(), ResourceGroupVH::getTags));

            ExecutorService executor = Executors.newCachedThreadPool();

            List<String> longRunningTargetTypeList = new CopyOnWriteArrayList<>();

            for (String targetType : TARGET_TYPES_TO_COLLECT) {
                executor.execute(() -> {
                    if (!(isTypeInScope(targetType))) {
                        return;
                    }
                    try {
                        longRunningTargetTypeList.add(targetType);
                        FileManager.generateTargetTypeFile(assetDataFactory.getAssetData(subscription, tagMap, targetType), targetType);
                        longRunningTargetTypeList.remove(targetType);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Util.eCount.getAndIncrement();
                    }
                });
            }

            executor.shutdown();
            try {
                while (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    log.error("Following target type collectors for subscription - {} have exceeded 1 hour - {}", subscription.getSubscriptionId(), String.join(" , ", longRunningTargetTypeList));
                }
            } catch (InterruptedException e) {
                log.error("Interrupted exception occurred in azure collector", e);
            }

            while (!executor.isTerminated()) {
            }

            log.info("Finished Discovery for sub {}", subscription);
        }

        triggerNotificationforPermissionDenied();

        //Below logger message is used by datadog to create alert.
        if (Util.eCount.get() > 0) {
            log.error(ERROR_PREFIX + "for at least one collector. Number of failures detected is " + Util.eCount.get(), new Exception("Error in at least one collector"));
        }
        if (connectedSubscriptions.isEmpty()) {
            rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline'", Collections.emptyList());
        } else {
            String combinedConnectedSubsStr = connectedSubscriptions.stream().map(sub -> "'" + sub + "'").collect(Collectors.joining(","));
            rdsdbManager.executeUpdate("UPDATE cf_AzureTenantSubscription SET subscriptionStatus='offline' WHERE subscription NOT IN (" + combinedConnectedSubsStr + ")", Collections.emptyList());
        }
        try {
            FileManager.finalise();
        } catch (Exception e) {
            log.error(ERROR_PREFIX + "while adding closing bracket to data files", e);
            System.exit(1);
        }
    }

    private Map<String, String> getRegionsFromAzure(SubscriptionVH subscription) {
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String regionTemplate = "https://management.azure.com/subscriptions/%s/locations?api-version=2020-01-01";
        String url = String.format(regionTemplate, URLEncoder.encode(subscription.getSubscriptionId()));
        Map<String, String> regionMap = new HashMap<>();
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray regions = responseObj.getAsJsonArray("value");
            for (JsonElement region : regions) {
                JsonObject regionObj = region.getAsJsonObject();
                regionMap.put(regionObj.get("displayName").getAsString(), regionObj.get("name").getAsString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return regionMap;
    }

    private boolean isTypeInScope(String type) {
        if ("".equals(targetTypes)) {
            return true;
        } else {
            List<String> targetTypesList = Arrays.asList(targetTypes.split(","));
            return targetTypesList.contains(type);
        }
    }
}
