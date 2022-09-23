package com.tmobile.pacbot.azure.inventory.collector;


import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.NetworkWatcherLogFlowVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class NetworkWatcherInventoryCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private static Logger log = LoggerFactory.getLogger(NSGInventoryCollector.class);
    public List<NetworkWatcherLogFlowVH> fetchNetworkWatcherLogsBySecurityGroupId(SubscriptionVH subscription, String nsgId){

        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());

        List<NetworkWatcherLogFlowVH> networkWatcherVHList = new ArrayList<>();
        PagedList<NetworkWatcher> networkWatcherPagedList=	azure.networkWatchers().list();
         if (networkWatcherPagedList.size() > 0) {
                for (NetworkWatcher networkWatcher : networkWatcherPagedList) {
                    NetworkWatcherLogFlowVH networkWatcherLogFlowVHVH=new NetworkWatcherLogFlowVH();
                    try {
                        networkWatcherLogFlowVHVH.setEnabled(networkWatcher.getFlowLogSettings(nsgId).enabled());
                        networkWatcherLogFlowVHVH.setId(networkWatcher.id());
                        networkWatcherLogFlowVHVH.setName(networkWatcher.name());
                        networkWatcherLogFlowVHVH.setEnabled(networkWatcher.getFlowLogSettings(nsgId).enabled());
                        networkWatcherLogFlowVHVH.setRetentionEnabled(networkWatcher.getFlowLogSettings(nsgId).isRetentionEnabled());
                        networkWatcherLogFlowVHVH.setRetentionInDays(networkWatcher.getFlowLogSettings(nsgId).retentionDays());
                        log.info("network watcher {}", networkWatcher.getFlowLogSettings(nsgId).networkSecurityGroupId());
                        networkWatcherVHList.add(networkWatcherLogFlowVHVH);
                    } catch (CloudException e) {

                    }


                }
            }
            return networkWatcherVHList;
        }
    }


