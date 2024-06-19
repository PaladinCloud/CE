package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.containerservice.KubernetesCluster;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.KubernetesClustersVH;
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
public class KubernetesServicesCollector implements Collector {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesServicesCollector.class);
    private final String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.ContainerService/managedClusters/%s?api-version=2022-09-01";
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<KubernetesClustersVH> collect(SubscriptionVH subscription) {
        List<KubernetesClustersVH> kubernetesClustersVHList = new ArrayList<>();

        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());


        PagedList<KubernetesCluster> kubernetesClusterList = azure.kubernetesClusters().list();

        logger.info("kubernetesClusterList size : {}  ", kubernetesClusterList.size());

        for (KubernetesCluster kubernetesCluster : kubernetesClusterList) {
            try {
                String url = String.format(apiUrlTemplate,
                        URLEncoder.encode(subscription.getSubscriptionId(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(kubernetesCluster.resourceGroupName(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(kubernetesCluster.name(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()));
                String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
                logger.info("subscriptionName: {}", subscription.getSubscriptionName());
                JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
                JsonObject kubernetesClusterObject = responseObj.getAsJsonObject();
                logger.info("kubernetesClusterObject: {}", kubernetesClusterObject);

                if (kubernetesClusterObject != null) {
                    KubernetesClustersVH kubernetesClustersVH = new KubernetesClustersVH();
                    kubernetesClustersVH.setVersion(kubernetesCluster.version().toString());
                    kubernetesClustersVH.setId(kubernetesCluster.id());
                    kubernetesClustersVH.setEnableRBAC(kubernetesCluster.enableRBAC());
                    String kubernatesClusterName = kubernetesClusterObject.get("name").getAsString() != null && !kubernetesClusterObject.get("name").getAsString().isEmpty() ? kubernetesClusterObject.get("name").getAsString() : "";
                    logger.info("kubernatesClusterName: {}", kubernatesClusterName);
                    kubernetesClustersVH.setName(kubernatesClusterName);
                    try {
                        if (kubernetesCluster.addonProfiles() != null && kubernetesCluster.addonProfiles().get("kubeDashboard") != null) {
                            kubernetesClustersVH.setDashBoardEnabled(kubernetesCluster.addonProfiles().get("kubeDashboard").enabled());
                        }
                    } catch (Exception exception) {
                        //Data alert will not be published for the below error.
                        logger.error(" Following error occurred while fetching add on profiles for kubernetes cluster - {}, This can be due to no addOnProfiles" +
                                "present for cluster. ", exception.getMessage());
                    }

                    kubernetesClustersVH.setSubscription(subscription.getSubscriptionId());
                    kubernetesClustersVH.setSubscriptionName(subscription.getSubscriptionName());
                    kubernetesClustersVH.setRegion(Util.getRegionValue(subscription, kubernetesClusterObject.get("location").getAsString()));
                    kubernetesClustersVH.setResourceGroupName(
                            kubernetesCluster.resourceGroupName());
                    kubernetesClustersVH.setTags(kubernetesCluster.tags());
                    JsonObject properties = kubernetesClusterObject.getAsJsonObject("properties");

                    if (properties != null) {
                        HashMap<String, Object> propertiesMap = new Gson().fromJson(
                                properties.toString(),
                                HashMap.class);
                        kubernetesClustersVH.setProperties(propertiesMap);
                    }

                    kubernetesClustersVHList.add(kubernetesClustersVH);
                }

            } catch (Exception exception) {
                String errorMessage = String.format("Error collecting KubernetesServices for subscriptionId: %s, subscriptionName: %s", subscription.getSubscriptionId(), subscription.getSubscriptionName());
                logger.error(errorMessage, exception);
                Util.eCount.getAndIncrement();
                logger.debug("Current error count after exception occurred in KubernetesServices Collector: {}", Util.eCount.get());

            }
        }
        return kubernetesClustersVHList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
