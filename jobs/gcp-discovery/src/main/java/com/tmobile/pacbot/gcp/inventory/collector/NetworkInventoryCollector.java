package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.compute.v1.Network;
import com.tmobile.pacbot.gcp.inventory.InventoryConstants;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.NetworkVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NetworkInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(NetworkInventoryCollector.class);
    public List<NetworkVH> fetchNetworkInventory(ProjectVH project) throws Exception {
        List<NetworkVH> networkVHList=new ArrayList<>();
        Iterable<Network> networkList = gcpCredentialsProvider.getNetworksClient().list(project.getProjectId()).iterateAll();
        for (Network network:networkList) {
            NetworkVH networkVH=new NetworkVH();
            networkVH.setId(String.valueOf(network.getId()));
            networkVH.setProjectName(project.getProjectName());
            networkVH.setProjectId(project.getProjectId());
            networkVH.setName(network.getName());
            logger.info("**** NetworkInventoryCollector --> {}",network.getName());
            networkVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);
            networkVHList.add(networkVH);

        }
        return networkVHList;

    }

}
