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

import com.tmobile.pacbot.gcp.inventory.vo.NodePoolVH;
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
            regions.addAll(gcPlocationUtil.getLocations(project.getProjectId()));
            regions.remove("us");
            regions.remove("global");
            logger.debug("Number of regions {}", regions.size());
            logger.debug("Regions are:{}", regions);

            for (String region : regions) {
                logger.info("### GKe cluster  clusterList  inside region {}", region);
                ClusterManagerClient clusterManagerClient = gcpCredentialsProvider.getClusterManagerClient();
                String parent="projects/"+project.getProjectId()+"/locations/"+region;
                ListClustersResponse clusterList=null;
                try {
                    clusterList=clusterManagerClient.listClusters(parent);
                    logger.info("cluster Size {}", clusterManagerClient.listClusters(parent).getClustersList());

                }
                catch (Exception e){
                    logger.info("Exception {}",e.getMessage());

                }


                for (Cluster cluster : clusterList.getClustersList()) {

                    GKEClusterVH gkeClusterVH = new GKEClusterVH();
                    gkeClusterVH.setId(cluster.getId());
                    gkeClusterVH.setProjectName(project.getProjectName());
                    gkeClusterVH.setProjectId(project.getProjectId());
                    gkeClusterVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);

                    gkeClusterVH.setRegion(cluster.getLocation());
                    if (cluster.getMasterAuthorizedNetworksConfig() != null) {
                        HashMap<String, Object> masterAuthorizedNetworksConfigMap = new Gson().fromJson(
                                cluster.getMasterAuthorizedNetworksConfig().toString(),
                                HashMap.class);

                        gkeClusterVH.setMasterAuthorizedNetworksConfig(masterAuthorizedNetworksConfigMap);
                    }

                    if (cluster.getDatabaseEncryption().getKeyName() != null) {
                        String keyName = new Gson().fromJson(
                                cluster.getDatabaseEncryption().getKeyName(), String.class);

                        gkeClusterVH.setKeyName(keyName);
                    }


                    String nodepoolParent = "projects/" + project.getProjectId() + "/locations/" + region + "/clusters/" + cluster.getName();
                    ListNodePoolsResponse listNodePools = null;
                    try {
                        listNodePools = clusterManagerClient.listNodePools(nodepoolParent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    List<NodePoolVH> nodePoolVHList = new ArrayList<>();
                    if (listNodePools != null) {
                        for (NodePool nodePool : listNodePools.getNodePoolsList()) {
                            NodePoolVH nodePoolVH=new NodePoolVH();
                            nodePoolVH.setAutoUpgrade(nodePool.getManagement().getAutoUpgrade());
                            if(nodePool.getConfig().getBootDiskKmsKey()!=null){
                                String bootDiskKmsKey=new Gson().fromJson(nodePool.getConfig().getBootDiskKmsKey(),String.class);
                                gkeClusterVH.setBootDiskKmsKey(bootDiskKmsKey);

                            }
                            nodePoolVH.setEnableIntegrityMonitoring(nodePool.getConfig().getShieldedInstanceConfig().getEnableIntegrityMonitoring());
                            nodePoolVH.setEnableSecureBoot(nodePool.getConfig().getShieldedInstanceConfig().getEnableSecureBoot());
                            nodePoolVHList.add(nodePoolVH);
                        }
                    }
                    gkeClusterVH.setNodePools(nodePoolVHList);

                    gkeClusterlist.add(gkeClusterVH);

                }
                logger.debug("##########ending########-> {}", gkeClusterlist);

            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
        return gkeClusterlist;
    }

}