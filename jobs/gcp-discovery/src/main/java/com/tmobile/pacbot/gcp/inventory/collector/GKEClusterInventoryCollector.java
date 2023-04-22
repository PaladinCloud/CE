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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    public List<GKEClusterVH> fetchGKEClusterInventory(ProjectVH project){
        List<GKEClusterVH> gkeClusterlist = new ArrayList<>();
        HashSet<GKEClusterVH> gkeClusterVHSet=new HashSet<>();
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
                ClusterManagerClient clusterManagerClient = gcpCredentialsProvider.getClusterManagerClient(project.getProjectId());
                String parent="projects/"+project.getProjectId()+"/locations/"+region;
                ListClustersResponse clusterList=null;
                try {
                    clusterList = clusterManagerClient.listClusters(parent);
                    logger.info("cluster Size {}", clusterManagerClient.listClusters(parent).getClustersList());

                } catch (Exception e) {
                    logger.info("Exception {}", e.getMessage());

                }

                if(clusterList!=null){
                    List<Cluster> clusters=clusterList.getClustersList();
                for (Cluster cluster : clusters) {
                    GKEClusterVH gkeClusterVH = new GKEClusterVH();
                    gkeClusterVH.setId(cluster.getId());
                    gkeClusterVH.setName(cluster.getName());
                    gkeClusterVH.setProjectName(project.getProjectName());
                    gkeClusterVH.setProjectId(project.getProjectId());
                    gkeClusterVH.setRegion(cluster.getLocation());
                    gkeClusterVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);
                    gkeClusterVH.setIPAlias(cluster.getIpAllocationPolicy().getUseIpAliases());
                    gkeClusterVH.setCloudLogging(cluster.getLoggingService());
                    gkeClusterVH.setCloudMonitoring(cluster.getMonitoringService());
                    gkeClusterVH.setTags(cluster.getResourceLabelsMap());
                    logger.info("monitoring services {} {}", cluster.getName(), cluster.getMonitoringService());
                    if (cluster.getAddonsConfig() != null && !cluster.getAddonsConfig().getAllFields().isEmpty()) {
                        gkeClusterVH.setDisableKubernetesDashBoard(cluster.getAddonsConfig().getKubernetesDashboard().getDisabled());
                    }
                    if (cluster.getPrivateClusterConfig() != null) {
                        gkeClusterVH.setEnablePrivateNodes(cluster.getPrivateClusterConfig().getEnablePrivateNodes());
                        gkeClusterVH.setEnablePrivateEndPoints(cluster.getPrivateClusterConfig().getEnablePrivateEndpoint());
                    } else {
                        gkeClusterVH.setEnablePrivateNodes(false);
                        gkeClusterVH.setEnablePrivateEndPoints(false);
                    }
                    gkeClusterVH.setClientKey(cluster.getMasterAuth().getClientKey());

                    gkeClusterVH.setVersion(cluster.getCurrentMasterVersion());
                    gkeClusterVH.setRegion(cluster.getLocation());

                    if (cluster.getMasterAuthorizedNetworksConfig() != null && !cluster.getMasterAuthorizedNetworksConfig().getAllFields().isEmpty()) {
                        logger.info("getMasterAuthorizedNetworksConfig {} ***********", cluster.getMasterAuthorizedNetworksConfig());
                        HashMap<String, Object> masterAuthorizedNetworksConfigMap = new HashMap<>();

                        cluster.getMasterAuthorizedNetworksConfig().getAllFields().forEach((fieldDescriptor, o) -> {
                                    logger.info("field Descriptor {} {}", fieldDescriptor.getName(), o);
                                    masterAuthorizedNetworksConfigMap.put(fieldDescriptor.getName(), o.toString());
                                }
                        );


                        gkeClusterVH.setMasterAuthorizedNetworksConfig(masterAuthorizedNetworksConfigMap);
                    }

                    if (cluster.getDatabaseEncryption().getKeyName() != null) {
                        String keyName = new Gson().fromJson(
                                cluster.getDatabaseEncryption().getKeyName(), String.class);

                        gkeClusterVH.setKeyName(keyName);
                    }

                    gkeClusterVH.setLegacyAuthorization(cluster.getLegacyAbac().getEnabled());
                    gkeClusterVH.setIntraNodeVisibility(cluster.getNetworkConfig().getEnableIntraNodeVisibility());


                    String nodepoolParent = "projects/" + project.getProjectId() + "/locations/" + region + "/clusters/" + cluster.getName();
                    ListNodePoolsResponse listNodePools = null;
                    try {
                        listNodePools = clusterManagerClient.listNodePools(nodepoolParent);
                    } catch (Exception e) {
                        logger.debug(e.getMessage());
                    }

                    List<NodePoolVH> nodePoolVHList = new ArrayList<>();
                    if (listNodePools != null) {
                        for (NodePool nodePool : listNodePools.getNodePoolsList()) {
                            NodePoolVH nodePoolVH = new NodePoolVH();
                            nodePoolVH.setAutoUpgrade(nodePool.getManagement().getAutoUpgrade());
                            nodePoolVH.setAutoRepair(nodePool.getManagement().getAutoRepair());
                            if (nodePool.getConfig().getBootDiskKmsKey() != null) {
                                String bootDiskKmsKey = new Gson().fromJson(nodePool.getConfig().getBootDiskKmsKey(), String.class);
                                gkeClusterVH.setBootDiskKmsKey(bootDiskKmsKey);

                            }
                            nodePoolVH.setEnableIntegrityMonitoring(nodePool.getConfig().getShieldedInstanceConfig().getEnableIntegrityMonitoring());
                            nodePoolVH.setEnableSecureBoot(nodePool.getConfig().getShieldedInstanceConfig().getEnableSecureBoot());

                            nodePoolVHList.add(nodePoolVH);
                        }
                    }
                    gkeClusterVH.setNodePools(nodePoolVHList);
                    gkeClusterVH.setEnableKubernetesAlpha(cluster.getEnableKubernetesAlpha());
                    gkeClusterVH.setPassword(cluster.getMasterAuth().getPassword());
                    gkeClusterVH.setUsername(cluster.getMasterAuth().getUsername());

                    gkeClusterVHSet.add(gkeClusterVH);

                }
                logger.debug("##########ending########-> {}", gkeClusterlist);
            }

            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
        gkeClusterlist.addAll(gkeClusterVHSet);
        return gkeClusterlist;
    }

}
