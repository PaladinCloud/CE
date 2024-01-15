package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.DefaultErrorResponseException;
import com.microsoft.azure.management.appservice.WebApp;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.util.ErrorManageUtil;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.WebAppVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WebAppInventoryCollector implements Collector {
    private static final Logger log = LoggerFactory.getLogger(WebAppInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    public List<WebAppVH> collect(SubscriptionVH subscription) {
        List<WebAppVH> webAppList = new ArrayList<>();
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<WebApp> webApps = azure.webApps().list();
        WebAppVH webAppVH = new WebAppVH();
        for (WebApp webApp : webApps) {
            try {
                webAppVH.setRemoteDebuggingEnabled(webApp.remoteDebuggingEnabled());
                webAppVH.setHostNames(webApp.hostNames());
                webAppVH.setHttp20Enabled(webApp.http20Enabled());
                webAppVH.setResourceGroupName(webApp.resourceGroupName());
                webAppVH.setSubscription(subscription.getSubscriptionId());
                webAppVH.setSubscriptionName(subscription.getSubscriptionName());
                webAppVH.setRegion(Util.getRegionValue(subscription, webApp.regionName()));
                webAppVH.setResourceGroupName(webApp.resourceGroupName());
                if (webApp.ftpsState() != null) {
                    webAppVH.setFtpsState(webApp.ftpsState());
                }
                if (webApp.minTlsVersion() != null) {
                    webAppVH.setMinTlsVersion(webApp.minTlsVersion().toString());
                }
                webAppVH.setAuthEnabled(webApp.getAuthenticationConfig().inner().enabled());
                webAppVH.setId(webApp.id());
                webAppVH.setHttpsOnly(webApp.httpsOnly());
                webAppVH.setClientCertEnabled(webApp.clientCertEnabled());
                webAppVH.setTags(webApp.tags());
                webAppVH.setSystemAssignedManagedServiceIdentityPrincipalId(webApp.systemAssignedManagedServiceIdentityPrincipalId());
                webAppVH.setName(webApp.name());
                webAppList.add(webAppVH);
            } catch (DefaultErrorResponseException e) {
                log.error("Error Collecting Web App info", e);
                ErrorManageUtil.uploadError(webAppVH.getSubscription(), webAppVH.getRegion(), "webapp", e.getMessage());
            } catch (Exception e) {
                log.error("Error Collecting Web App info", e);
                Util.eCount.getAndIncrement();
            }
        }

        return webAppList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}
