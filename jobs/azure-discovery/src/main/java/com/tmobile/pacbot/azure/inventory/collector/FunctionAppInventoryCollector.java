package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.FunctionAppVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FunctionAppInventoryCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private static Logger log = LoggerFactory.getLogger(FunctionAppInventoryCollector.class);

    public List<FunctionAppVH> fetchFunctionAppDetails(SubscriptionVH subscription) {
        List<FunctionAppVH> functionAppList = new ArrayList();
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());
        PagedList<FunctionApp> functionApps = azure.appServices().functionApps().list();
        for(FunctionApp functionApp:functionApps){
            FunctionAppVH functionAppVH=new FunctionAppVH();
            functionAppVH.setId(functionApp.id());
            functionAppVH.setSubscription(subscription.getSubscription());
            functionAppVH.setResourceGroupName(subscription.getResourceGroupName());
            functionAppVH.setClientCertEnabled(functionApp.clientCertEnabled());
            functionAppList.add(functionAppVH);
        }
        return functionAppList;

    }
}
