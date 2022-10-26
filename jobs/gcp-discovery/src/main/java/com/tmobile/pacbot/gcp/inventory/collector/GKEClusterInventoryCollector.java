package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.container.v1.ListNodePoolsResponse;
import com.google.container.v1.NodePool;
import com.tmobile.pacbot.gcp.inventory.vo.GKEClusterVH;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.container.v1.Cluster;
import com.google.container.v1.ListClustersResponse;
import com.google.gson.Gson;
import com.tmobile.pacbot.gcp.inventory.InventoryConstants;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.util.GCPlocationUtil;

import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
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

    public List<GKEClusterVH> fetchGKEClusterInventory(ProjectVH project) throws IOException, GeneralSecurityException {
        List<GKEClusterVH> gkeClusterlist = new ArrayList<>();
        logger.info("### GKe cluster  collector ###########");
        try {
            List<String> regions = gcPlocationUtil.getZoneList(project.getProjectId());
            regions.remove("us");
            regions.remove("global");
            logger.debug("Number of regions {}", regions.size());
            logger.debug("Regions are:{}", regions);

            for (String region : regions) {
                logger.info("### GKe cluster  clusterList  inside region {}", region);
                ClusterManagerClient clusterManagerClient = gcpCredentialsProvider.getClusterManagerClient();

                ListClustersResponse clusterList = clusterManagerClient.listClusters(project.getProjectId(), region);
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

                    if(cluster.getDatabaseEncryption().getKeyName()!=null) {
                        String keyName = new Gson().fromJson(
                                cluster.getDatabaseEncryption().getKeyName(), String.class);

                        gkeClusterVH.setKeyName(keyName);
                    }

                    String clusterId=cluster.getId();
                    logger.info("### Gke cluster clusterid",clusterId);

                    ListNodePoolsResponse listNodePools =clusterManagerClient.listNodePools(project.getProjectId(), region, clusterId);
                    logger.info("### GKe cluster NodePoolList ########### ");
                    logger.info("Nodepool size {}", listNodePools.getNodePoolsCount());
                    List<Boolean> nodePoolIntegrityMonitoring=new ArrayList();
                    for(NodePool nodePool:listNodePools.getNodePoolsList()){

                        if(nodePool.getConfig().getBootDiskKmsKey()!=null){
                            String bootDiskKmsKey=new Gson().fromJson(nodePool.getConfig().getBootDiskKmsKey(),String.class);
                            gkeClusterVH.setBootDiskKmsKey(bootDiskKmsKey);
                        }
                        nodePoolIntegrityMonitoring.add(nodePool.getConfig().getShieldedInstanceConfig().getEnableIntegrityMonitoring());
                    }
                    gkeClusterVH.setNodePoolIntegrityMonitoring(nodePoolIntegrityMonitoring);
                    gkeClusterVH.setId(String.valueOf(cluster.getId()));
                    gkeClusterVH.setProjectName(project.getProjectName());
                    gkeClusterVH.setProjectId(project.getProjectId());
                    gkeClusterVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);

                    gkeClusterVH.setRegion(cluster.getLocation());
                    gkeClusterlist.add(gkeClusterVH);
                }
                logger.debug("##########ending########-> {}", gkeClusterlist);

                clusterManagerClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
        return gkeClusterlist;
    }

}
