package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.compute.v1.*;
import com.tmobile.pacbot.gcp.inventory.vo.*;
import org.slf4j.LoggerFactory;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VMInventoryCollector {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(VMInventoryCollector.class);

    public List<VirtualMachineVH> fetchInstanceInventory(ProjectVH project) throws IOException {
        List<VirtualMachineVH> instanceList = new ArrayList<>();
        logger.debug("Project id:{}",project.getProjectNumber());
        InstancesClient instancesClient = gcpCredentialsProvider.getInstancesClient();

        AggregatedListInstancesRequest aggregatedListInstancesRequest = AggregatedListInstancesRequest
                .newBuilder()
                .setProject(project.getProjectId())
                .build();

        InstancesClient.AggregatedListPagedResponse response = instancesClient
                .aggregatedList(aggregatedListInstancesRequest);

        for (Map.Entry<String, InstancesScopedList> zoneInstances : response.iterateAll()) {
            // Instances scoped by each zone
            String zone = zoneInstances.getKey();
            if (!zoneInstances.getValue().getInstancesList().isEmpty()) {
                // zoneInstances.getKey() returns the fully qualified address.
                // Hence, strip it to get the zone name only
                String zoneName = zone.substring(zone.lastIndexOf("/") + 1);
                logger.debug("Instances at %s: {} ", zoneName);
                for (Instance instance : zoneInstances.getValue().getInstancesList()) {
                    try {
                        logger.debug((instance.getName() + " " + instance.getCreationTimestamp()));

                        VirtualMachineVH virtualMachineVH = new VirtualMachineVH();
                        virtualMachineVH.setId(String.valueOf(instance.getId()));
                        virtualMachineVH.setMachineType(instance.getMachineType());
                        virtualMachineVH.setTags(instance.getLabelsMap());
                        virtualMachineVH.setProjectName(project.getProjectName());
                        virtualMachineVH.setProjectId(project.getProjectId());
                        virtualMachineVH.setName(instance.getName());
                        virtualMachineVH.setDescription(instance.getDescription());
                        virtualMachineVH.setRegion(zoneName);
                        virtualMachineVH.setStatus(instance.getStatus());
                        virtualMachineVH.setServiceAccounts(getServiceAccountList(instance.getServiceAccountsList()));
                        logger.info("On hoost maintainenece attribute");
                        virtualMachineVH.setOnHostMaintainence(instance.getScheduling().getOnHostMaintenance());
                        virtualMachineVH.setProjectNumber(project.getProjectNumber().toString());
                        virtualMachineVH.setConfidentialComputing(instance.getConfidentialInstanceConfigOrBuilder().getEnableConfidentialCompute());
                        this.setShieldedConfig(instance,virtualMachineVH);
                        this.setItemList(instance,virtualMachineVH);
                        this.setVMDisks(instance, virtualMachineVH);
                        this.setNetworkInterfaces(instance, virtualMachineVH);
                        this.setScopeList(instance,virtualMachineVH);
                        instanceList.add(virtualMachineVH);
                    } catch (Exception e) {
                        logger.error("Error while fetching instance inventory for {} {}", instance.getName(), e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        return instanceList;
    }

    private void setScopeList(Instance instance, VirtualMachineVH virtualMachineVH) {
        List collectScopeList =new ArrayList();
        List emailList=new ArrayList();
        for(ServiceAccount serviceAccount:instance.getServiceAccountsList())
        {
            logger.debug("Scope list{}",serviceAccount.getScopesList());
            logger.debug("Email:{}",serviceAccount.getEmail());
            List scopeList= Arrays.asList(serviceAccount.getScopesList().toArray());
            collectScopeList.addAll(scopeList);
            emailList.add(serviceAccount.getEmail());
        }
        virtualMachineVH.setScopesList(collectScopeList);
        virtualMachineVH.setEmailList(emailList);
    }

    public List<HashMap<String,Object>> getServiceAccountList(List<ServiceAccount> serviceAccountList){
        List<HashMap<String,Object>>resList=new ArrayList<>();
        for (ServiceAccount serviceAccount:serviceAccountList) {
            HashMap<String,Object>serviceAccountMap=new HashMap<>();
            serviceAccountMap.put("email",serviceAccount.getEmail());
            serviceAccountMap.put("emailBytes",serviceAccount.getEmailBytes());
            serviceAccountMap.put("scopeList",serviceAccount.getScopesList());
            resList.add(serviceAccountMap);
        }
        return  resList;
    }

    private void setShieldedConfig(Instance instance, VirtualMachineVH virtualMachineVH) {
        ShieldedInstanceConfigVH shieldedInstanceConfigVH=new ShieldedInstanceConfigVH();
        if(instance.hasShieldedInstanceConfig()) {
            shieldedInstanceConfigVH.setEnableVtpm(instance.getShieldedInstanceConfig().getEnableVtpm());
            shieldedInstanceConfigVH.setEnableIntegrityMonitoring(instance.getShieldedInstanceConfig().getEnableIntegrityMonitoring());
        }
        virtualMachineVH.setShieldedInstanceConfig(shieldedInstanceConfigVH);
    }

    private void setItemList(Instance instance, VirtualMachineVH virtualMachineVH) {
        List<ItemInterfaceVH> itemsVH = new ArrayList<>();
        for (Items item : instance.getMetadata().getItemsList()) {
            ItemInterfaceVH itemVH=new ItemInterfaceVH();
            itemVH.setKey(item.getKey());
            itemVH.setValue(item.getValue());
            itemsVH.add(itemVH);
    }
        virtualMachineVH.setItems(itemsVH);
    }

    private void setNetworkInterfaces(Instance instance, VirtualMachineVH virtualMachineVH) {
        List<NetworkInterfaceVH> networkInterfaceVHList = new ArrayList<>();
        for (NetworkInterface networkInterface : instance.getNetworkInterfacesList()) {
            NetworkInterfaceVH networkInterfaceVH = new NetworkInterfaceVH();
            networkInterfaceVH.setName(networkInterface.getName());
            networkInterfaceVH.setNetwork(networkInterface.getNetwork());
            networkInterfaceVH.setId(networkInterface.getName());

            List<AccessConfigVH> accessConfigVHList = new ArrayList<>();
            for (AccessConfig accessConfig : networkInterface.getAccessConfigsList()) {
                AccessConfigVH accessConfigVH = new AccessConfigVH();
                accessConfigVH.setId(accessConfig.getName());
                accessConfigVH.setName(accessConfig.getName());
                accessConfigVH.setNatIP(accessConfig.getNatIP());
                accessConfigVH.setProjectName(virtualMachineVH.getProjectName());
                accessConfigVHList.add(accessConfigVH);
            }
            networkInterfaceVH.setAccessConfigs(accessConfigVHList);
            networkInterfaceVHList.add(networkInterfaceVH);
        }
        virtualMachineVH.setNetworkInterfaces(networkInterfaceVHList);
    }

    private void setVMDisks(Instance vmInstance, VirtualMachineVH vm) {
        List<VMDiskVH> diskVHS = new ArrayList<>();
        List<AttachedDisk> disksList = vmInstance.getDisksList();
        // convert AttachedDisk into VMDiskVH
        disksList.forEach(disk -> {
            VMDiskVH diskVH = new VMDiskVH();
            diskVH.setId(String.valueOf(disk.getIndex()));
            diskVH.setName(disk.getDeviceName());
            diskVH.setSizeInGB(disk.getDiskSizeGb());
            diskVH.setType(disk.getType());
            diskVH.setHasSha256(disk.getDiskEncryptionKey().hasSha256());
            diskVH.setHasKmsKeyName(disk.getDiskEncryptionKey().hasKmsKeyName());
            diskVH.setProjectName(vm.getProjectName());
            diskVHS.add(diskVH);
        });
        vm.setDisks(diskVHS);
    }
}
