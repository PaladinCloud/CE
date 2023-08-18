package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.VirtualMachineScaleSetVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VirtualMachineScaleSetCollector {

    @Autowired
    AzureCredentialProvider azureCredentialProvider;
    private String apiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Compute/virtualMachines/%s?api-version=2023-03-01";
    private static Logger log = LoggerFactory.getLogger(VirtualMachineScaleSetCollector.class);

    public List<VirtualMachineScaleSetVH> fetchVMScaleSetDetails(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {

        List<VirtualMachineScaleSetVH> vmssList = new ArrayList<>();

        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<VirtualMachineScaleSet> vmss = azure.virtualMachineScaleSets().list();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

        for (VirtualMachineScaleSet virtualMachineScaleSet : vmss) {
            try {
                String url = String.format(apiUrlTemplate,
                        URLEncoder.encode(subscription.getSubscriptionId(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(virtualMachineScaleSet.resourceGroupName(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(virtualMachineScaleSet.name(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()));
                JsonObject virtualMachineScaleSetObject = getVirtualMachineScaleSetObject(subscription, accessToken, virtualMachineScaleSet, url);
                log.info("virtualMachineScaleSetObject: {}", virtualMachineScaleSetObject);


                VirtualMachineScaleSetVH virtualMachineScaleSetVH = new VirtualMachineScaleSetVH();

                virtualMachineScaleSetVH.setComputerName(virtualMachineScaleSet.computerNamePrefix() == null
                        ?virtualMachineScaleSet.computerNamePrefix():virtualMachineScaleSet.name());
                virtualMachineScaleSetVH.setName(virtualMachineScaleSet.name());
                virtualMachineScaleSetVH.setRegion(Util.getRegionValue(subscription,virtualMachineScaleSet.regionName()));
                virtualMachineScaleSetVH.setSubscription(subscription.getSubscriptionId());
                virtualMachineScaleSetVH.setSubscriptionName(subscription.getSubscriptionName());
                virtualMachineScaleSetVH.setResourceGroupName(virtualMachineScaleSet.resourceGroupName());
                virtualMachineScaleSetVH.setId(virtualMachineScaleSet.id());
                virtualMachineScaleSetVH.setTags(Util.tagsList(tagMap, virtualMachineScaleSet.resourceGroupName(), virtualMachineScaleSet.tags()));

                List<VirtualMachineScaleSetVM> instanceList = virtualMachineScaleSet.virtualMachines().list();
                List<String> vmIds = new ArrayList<>();
                for (VirtualMachineScaleSetVM instance : instanceList) {
                    vmIds.add(instance.id());
                }
                virtualMachineScaleSetVH.setVirtualMachineIds(vmIds);
                List<VirtualMachineScaleSetNetworkConfiguration> networkConfigurationList = virtualMachineScaleSet.networkProfile().networkInterfaceConfigurations();
                for (VirtualMachineScaleSetNetworkConfiguration networkConfiguration : networkConfigurationList) {
                    List<VirtualMachineScaleSetIPConfiguration> ipConfigurations = networkConfiguration.ipConfigurations();
                    for (VirtualMachineScaleSetIPConfiguration ipConfiguration : ipConfigurations) {
                        List<SubResource> backendAddressPools = ipConfiguration.loadBalancerBackendAddressPools();
                        List<String> lbIds = new ArrayList<>();
                        for (SubResource resource : backendAddressPools) {
                            lbIds.add(resource.id());
                        }
                        virtualMachineScaleSetVH.setLoadBalancerIds(lbIds);
                    }
                }
                if(virtualMachineScaleSetObject!=null) {
                    JsonObject properties = virtualMachineScaleSetObject.getAsJsonObject("properties");
                    if (properties != null) {
                        HashMap<String, Object> propertiesMap = new Gson().fromJson(
                                properties.toString(),
                                HashMap.class);
                        virtualMachineScaleSetVH.setProperties(propertiesMap);
                    }
                    vmssList.add(virtualMachineScaleSetVH);
                }
            }catch(Exception e) {
                log.error("Error Collecting info for {} {} ",virtualMachineScaleSet.computerNamePrefix(),e.getMessage());
                Util.eCount.getAndIncrement();
            }
        }
        return vmssList;
    }

    private static JsonObject getVirtualMachineScaleSetObject(SubscriptionVH subscription, String accessToken, VirtualMachineScaleSet virtualMachineScaleSet, String url) {
        JsonObject getVirtualMachineScaleSetObject =null;
        try {
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            log.info("response form API: {} : {}",
                    response,
                    virtualMachineScaleSet.name().isEmpty() ? virtualMachineScaleSet.name() : "");
            log.info("subscriptionName: {}", subscription.getSubscriptionName());
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            getVirtualMachineScaleSetObject = responseObj.getAsJsonObject();
        }catch (Exception e){
            log.error("Error in fetching VM info from API {}, Error:{} ", url,e.getMessage());
        }
        return getVirtualMachineScaleSetObject;
    }
}
