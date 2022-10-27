package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.Gson;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.CosmosDBVH;
import com.tmobile.pacbot.azure.inventory.vo.KubernettesClustersVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Component
public class KubernettesServicesCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;
    private static Logger log = LoggerFactory.getLogger(KubernettesServicesCollector.class);
    public List<KubernettesClustersVH> fetchKubernetesClusterDetails(SubscriptionVH subscription) {
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());
        KubernettesClustersVH kubernettesClustersVH= new KubernettesClustersVH();
        kubernettesClustersVH.setId(azure.kubernetesClusters().list().get(0).id());
        kubernettesClustersVH.setEnableRBAC(azure.kubernetesClusters().list().get(0).enableRBAC());
        kubernettesClustersVH.setSubscription(subscription.getSubscriptionId());
        kubernettesClustersVH.setSubscriptionName(subscription.getSubscriptionName());
        List<KubernettesClustersVH> kclustersVHList = new ArrayList<>();
        kclustersVHList.add(kubernettesClustersVH);
        return kclustersVHList;
    }
}
