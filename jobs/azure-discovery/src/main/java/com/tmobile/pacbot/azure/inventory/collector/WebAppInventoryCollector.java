package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.DefaultErrorResponseException;
import com.microsoft.azure.management.appservice.WebApp;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.util.ErrorManageUtil;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.WebAppVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebAppInventoryCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private static Logger log = LoggerFactory.getLogger(WebAppInventoryCollector.class);

    public List<WebAppVH> fetchWebAppDetails(SubscriptionVH subscription) {
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
                    log.info("ftpsState {}", webApp.ftpsState());
                }
                if (webApp.minTlsVersion() != null) {
                    webAppVH.setMinTlsVersion(webApp.minTlsVersion().toString());
                    log.info("minTlsVersion {}", webApp.minTlsVersion());
                }
                webAppVH.setAuthEnabled(webApp.getAuthenticationConfig().inner().enabled());
                webAppVH.setId(webApp.id());
                webAppVH.setHttpsOnly(webApp.httpsOnly());
                webAppVH.setClientCertEnabled(webApp.clientCertEnabled());
                log.info("web app client cert {}", webApp.clientCertEnabled());
                webAppVH.setTags(webApp.tags());
                webAppVH.setSystemAssignedManagedServiceIdentityPrincipalId(webApp.systemAssignedManagedServiceIdentityPrincipalId());
                webAppVH.setName(webApp.name());
                webAppList.add(webAppVH);

            } catch (DefaultErrorResponseException exception) {
                log.error(exception.getMessage());
                ErrorManageUtil.uploadError(webAppVH.getSubscription(), webAppVH.getRegion(), "webapp", exception.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error Collecting info for {} ", e.getMessage());
                Util.eCount.getAndIncrement();
            }
        }
        return webAppList;
    }
}
