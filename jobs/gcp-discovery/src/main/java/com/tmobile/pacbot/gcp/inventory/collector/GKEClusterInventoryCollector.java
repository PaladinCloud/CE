package com.tmobile.pacbot.gcp.inventory.collector;

import com.tmobile.pacbot.gcp.inventory.vo.GKEClusterVH;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.container.v1.Cluster;
import com.google.container.v1.ListClustersResponse;
import com.google.gson.Gson;
import com.tmobile.pacbot.gcp.inventory.InventoryConstants;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.util.GCPlocationUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Component;

import org.slf4j.LoggerFactory;

@Component
public class GKEClusterInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    @Autowired
    GCPlocationUtil gcPlocationUtil;
    private static final Logger logger = LoggerFactory.getLogger(GKEClusterInventoryCollector.class);

    public List<GKEClusterVH> fetchGKEClusterInventory(String projectId) throws IOException, GeneralSecurityException {
        List<GKEClusterVH> gkeClusterlist = new ArrayList<>();
        logger.info("### GKe cluster  collector ###########");
        try {
            List<String> regions = gcPlocationUtil.getZoneList(projectId);
            regions.remove("us");
            regions.remove("global");
            logger.debug("Number of regions {}", regions.size());
            logger.debug("Regions are:{}", regions);

            for (String region : regions) {
                logger.info("### GKe cluster  clusterList  inside region {}", region);
                ClusterManagerClient clusterManagerClient = gcpCredentialsProvider.getClusterManagerClient();

                ListClustersResponse clusterList = clusterManagerClient.listClusters(projectId, region);
                logger.info("### GKe cluster clusterList ########### ");
                logger.info("cluster size {}", clusterList.getClustersCount());

                for (Cluster cluster : clusterList.getClustersList()) {
                    GKEClusterVH gkeClusterVH = new GKEClusterVH();

                    if (cluster.getMasterAuthorizedNetworksConfig() != null) {
                        HashMap<String, Object> masterAuthorizedNetworksConfigMap = new Gson().fromJson(
                                cluster.getMasterAuthorizedNetworksConfig().toString(),
                                HashMap.class);

                        gkeClusterVH.setMasterAuthorizedNetworksConfig(masterAuthorizedNetworksConfigMap);
                    }
                    gkeClusterVH.setId(String.valueOf(cluster.getId()));
                    gkeClusterVH.setProjectName(projectId);
                    gkeClusterVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);

                    gkeClusterVH.setRegion(cluster.getLocation());
                    gkeClusterlist.add(gkeClusterVH);
                }
                logger.debug("##########ending########-> {}", gkeClusterlist);

                clusterManagerClient.close();
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
        return gkeClusterlist;

    }

}
