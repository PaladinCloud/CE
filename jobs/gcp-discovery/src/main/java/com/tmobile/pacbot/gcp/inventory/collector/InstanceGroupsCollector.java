package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.compute.v1.*;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.util.GCPlocationUtil;
import com.tmobile.pacbot.gcp.inventory.vo.ManagedInstanceGroupVH;
import com.tmobile.pacbot.gcp.inventory.vo.ManagedInstanceVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InstanceGroupsCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    @Autowired
    GCPlocationUtil gcPlocationUtil;
    private static final Logger logger = LoggerFactory.getLogger(InstanceGroupsCollector.class);
    public List<ManagedInstanceGroupVH> fetchInstanceGroupInventory(ProjectVH project) throws IOException {
        List<ManagedInstanceGroupVH>  managedInstanceGroup = new ArrayList<>();
        logger.info("in fetchInstanceGroupInventory services ********");
        try{
            InstanceGroupsClient instanceGroupsClient = gcpCredentialsProvider.getInstanceGroupsClient();
            InstanceGroupManagersClient insgtanceGrpManagersClient = gcpCredentialsProvider.getInstanceGroupManagersClient();
            List<String> zones = gcPlocationUtil.getZoneList(project.getProjectId());
            if(!CollectionUtils.isEmpty(zones))
                zones = zones.stream().distinct().collect(Collectors.toList());
            for(String zone : zones) {
                InstanceGroupsClient.ListPagedResponse instanceGroupResponse = instanceGroupsClient.list(project.getProjectId(), zone);
                if(instanceGroupResponse != null){
                    managedInstanceGroup = getManagedInstanceGroupVH(managedInstanceGroup, instanceGroupResponse, insgtanceGrpManagersClient,
                            project.getProjectId(), zone);
                }
            }
            instanceGroupsClient.close();
        }catch(Exception e){
            logger.error("Error occurred in InstanceGroupsCollector {} ", e.getMessage());
        }
        return managedInstanceGroup;
    }

    private List<ManagedInstanceGroupVH> getManagedInstanceGroupVH(List<ManagedInstanceGroupVH>  managedInstanceGroup,
                InstanceGroupsClient.ListPagedResponse instanceGroupResponse, InstanceGroupManagersClient insgtanceGrpManagersClient, String project, String zone ){
        for(InstanceGroup instanceGroup : instanceGroupResponse.iterateAll()){
            ManagedInstanceGroupVH managedInstanceGroupVH = new ManagedInstanceGroupVH();
            managedInstanceGroupVH.setName(instanceGroup.getName());
            managedInstanceGroupVH.setId(String.valueOf(instanceGroup.getId()));
            managedInstanceGroupVH.setProjectId(instanceGroup.getName());
            managedInstanceGroupVH.setProjectName("Paladin Cloud");
            managedInstanceGroupVH.setRegion(zone);
            InstanceGroupManagersClient.ListManagedInstancesPagedResponse managedInstancesResponse = insgtanceGrpManagersClient
                    .listManagedInstances(project, zone, instanceGroup.getName());
            if(managedInstancesResponse != null){
                managedInstanceGroupVH.setManagedInstance(getManagedInstanceVHList(managedInstancesResponse));
            }
            managedInstanceGroup.add(managedInstanceGroupVH);
        }
        return managedInstanceGroup;
    }

    private List<ManagedInstanceVH> getManagedInstanceVHList(InstanceGroupManagersClient.ListManagedInstancesPagedResponse managedInstancesResponse ){
        List<ManagedInstanceVH> managedInstanceList = new ArrayList<ManagedInstanceVH>();
        for(ManagedInstance managedInstance : managedInstancesResponse.iterateAll()){
            managedInstanceList.add(getManagedInstanceVH(managedInstance));
        }
        return managedInstanceList;
    }

    private ManagedInstanceVH getManagedInstanceVH(ManagedInstance managedInstance){
        ManagedInstanceVH instance = new ManagedInstanceVH();
        instance.setInstanceHealthCount(managedInstance.getInstanceHealthCount());
        if(!CollectionUtils.isEmpty(managedInstance.getInstanceHealthList())){
            List<String> healthCheckList = managedInstance.getInstanceHealthList().stream().map(ManagedInstanceInstanceHealth::getHealthCheck).collect(Collectors.toList());
            instance.setInstanceHealth(healthCheckList);
            instance.setName(managedInstance.getInstance());
        }
        return instance;
    }
}
