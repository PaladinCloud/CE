package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.FunctionAppVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FunctionAppInventoryCollector implements Collector {
    private static final Logger log = LoggerFactory.getLogger(FunctionAppInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<FunctionAppVH> collect(SubscriptionVH subscription) {
        List<FunctionAppVH> functionAppList = new ArrayList<>();
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<FunctionApp> functionApps = azure.appServices().functionApps().list();
        for (FunctionApp functionApp : functionApps) {
            FunctionAppVH functionAppVH = new FunctionAppVH();
            functionAppVH.setId(functionApp.id());
            functionAppVH.setSubscription(subscription.getSubscriptionId());
            functionAppVH.setSubscriptionName(subscription.getSubscriptionName());
            functionAppVH.setResourceGroupName(functionApp.resourceGroupName());
            functionAppVH.setName(functionApp.name());
            functionAppVH.setClientCertEnabled(functionApp.clientCertEnabled());
            functionAppVH.setRegion(Util.getRegionValue(subscription, functionApp.regionName()));
            functionAppVH.setTags(functionApp.tags());
            functionAppList.add(functionAppVH);

        }

        return functionAppList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
