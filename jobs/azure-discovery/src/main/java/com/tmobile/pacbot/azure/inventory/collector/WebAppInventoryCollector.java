package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.NetworkInterface;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.VirtualMachineVH;
import com.tmobile.pacbot.azure.inventory.vo.WebAppVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WebAppInventoryCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;


    private static Logger log = LoggerFactory.getLogger(WebAppInventoryCollector.class);
    public List<WebAppVH> fetchWebAppDetails(SubscriptionVH subscription) {
        List<WebAppVH> webAppList = new ArrayList<>();

        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());

        PagedList<WebApp> webApps = azure.webApps().list();

        for (WebApp webApp : webApps) {
            try {
                WebAppVH webAppVH = new WebAppVH();
                webAppVH.setRemoteDebuggingEnabled(webApp.remoteDebuggingEnabled());
                webAppVH.setHostNames(webApp.hostNames());
                webAppVH.setHttp20Enabled(webApp.http20Enabled());
                webAppVH.setResourceGroupName(webApp.resourceGroupName());
                webAppVH.setSubscription(subscription.getSubscriptionId());
                webAppVH.setSubscriptionName(subscription.getSubscriptionName());
                webAppList.add(webAppVH);
            }catch (Exception e)
            {
                e.printStackTrace();
                log.error("Error Collecting info for {} ",e.getMessage());
            }
        }
    return webAppList;
    }
}
