package com.tmobile.pacbot.azure.inventory.collector;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.OSDisk;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.Subnet;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacbot.azure.inventory.vo.VMDiskVH;
import com.tmobile.pacbot.azure.inventory.vo.VirtualMachineVH;

@Component
public class VMInventoryCollector {

	@Autowired
	AzureCredentialProvider azureCredentialProvider;

	private String apiUrlTemplate="https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Compute/disks/%s?api-version=2021-12-01";


	private static Logger log = LoggerFactory.getLogger(VMInventoryCollector.class);

	public List<VirtualMachineVH> fetchVMDetails(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
		List<VirtualMachineVH> vmList = new ArrayList<>();

		Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());
		
		List<NetworkInterface> networkInterfaces = azure.networkInterfaces().list();
		String accessToken = azureCredentialProvider.getToken(subscription.getTenant());

		PagedList<VirtualMachine> vms = azure.virtualMachines().list();
		for (VirtualMachine virtualMachine : vms) {
			try {
				 String url = String.format(apiUrlTemplate,
						URLEncoder.encode(subscription.getSubscriptionId(),
								java.nio.charset.StandardCharsets.UTF_8.toString()),
						URLEncoder.encode(virtualMachine.resourceGroupName(),
								java.nio.charset.StandardCharsets.UTF_8.toString()),
						URLEncoder.encode(virtualMachine.name(),
								java.nio.charset.StandardCharsets.UTF_8.toString()));

				String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
				log.info("response form API: {} : {}",
						response,
						virtualMachine.name().isEmpty() ? virtualMachine.name() : "");
				log.info("subscriptionName: {}", subscription.getSubscriptionName());
				JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
				JsonObject virtualMachineObject = responseObj.getAsJsonObject();
				log.info("virtualMachineObject: {}", virtualMachineObject);

				     if(virtualMachineObject!=null){

						 VirtualMachineVH vmVH = new VirtualMachineVH();

						 vmVH.setComputerName(virtualMachine.computerName() == null
								 ? virtualMachine.instanceView().computerName() == null ? virtualMachine.name()
								 : virtualMachine.instanceView().computerName()
								 : virtualMachine.computerName());
						 vmVH.setName(virtualMachine.name());
						 vmVH.setRegion(virtualMachine.regionName());
						 vmVH.setSubscription(subscription.getSubscriptionId());
						 vmVH.setSubscriptionName(subscription.getSubscriptionName());

						 virtualMachine.inner().networkProfile();
						 vmVH.setVmSize(virtualMachine.size().toString());
						 vmVH.setResourceGroupName(virtualMachine.resourceGroupName());

						 vmVH.setStatus(virtualMachine.powerState() != null
								 ? virtualMachine.powerState().toString().replace("PowerState/", "")
								 : "Unknown");

						 if(virtualMachine.instanceView()!=null) {
							 vmVH.setOs(virtualMachine.instanceView().osName());
							 vmVH.setOsVersion(virtualMachine.instanceView().osVersion());
						 }
						 vmVH.setOsType(virtualMachine.osType()!=null?virtualMachine.osType().toString():"");

						 vmVH.setNetworkInterfaceIds(virtualMachine.networkInterfaceIds());
						 vmVH.setAvailabilityZones(virtualMachine.availabilityZones());

						 vmVH.setVmId(virtualMachine.vmId());
						 vmVH.setManagedDiskEnabled(virtualMachine.isManagedDiskEnabled());

						 vmVH.setPrivateIpAddress(virtualMachine.getPrimaryNetworkInterface().primaryPrivateIP());
						 vmVH.setPublicIpAddress(virtualMachine.getPrimaryPublicIPAddress() != null
								 ? virtualMachine.getPrimaryPublicIPAddress().ipAddress()
								 : "");

						 vmVH.setAvailabilitySetId(virtualMachine.availabilitySetId());
						 vmVH.setProvisioningState(virtualMachine.provisioningState());
						 vmVH.setLicenseType(virtualMachine.licenseType());
						 vmVH.setId(virtualMachine.id());

						 vmVH.setBootDiagnosticsEnabled(virtualMachine.isBootDiagnosticsEnabled());
						 vmVH.setBootDiagnosticsStorageUri(virtualMachine.bootDiagnosticsStorageUri());
						 vmVH.setManagedServiceIdentityEnabled(virtualMachine.isManagedServiceIdentityEnabled());
						 vmVH.setSystemAssignedManagedServiceIdentityTenantId(
								 virtualMachine.systemAssignedManagedServiceIdentityTenantId());
						 vmVH.setSystemAssignedManagedServiceIdentityPrincipalId(
								 virtualMachine.systemAssignedManagedServiceIdentityPrincipalId());
						 vmVH.setUserAssignedManagedServiceIdentityIds(virtualMachine.userAssignedManagedServiceIdentityIds());
						 vmVH.setTags(Util.tagsList(tagMap, virtualMachine.resourceGroupName(), virtualMachine.tags()));
						 vmVH.setPrimaryNetworkIntefaceId(virtualMachine.primaryNetworkInterfaceId());
						 vmVH.setPrimaryNCIMacAddress(virtualMachine.getPrimaryNetworkInterface().macAddress());

						 setVmDisks(virtualMachine, vmVH);
						 setNsgs(virtualMachine, vmVH, networkInterfaces);
						 setVnetInfo(virtualMachine, vmVH);
						 setOtherVnets(virtualMachine, vmVH, networkInterfaces);

						 if (virtualMachine.osProfile() != null) {
							 if (virtualMachine.osProfile().linuxConfiguration() != null) {
								 vmVH.setPasswordBasedAuthenticationDisabled(
										 virtualMachine.osProfile().linuxConfiguration().disablePasswordAuthentication());
							 }
						 }

						 if (virtualMachine.listExtensions() != null) {
							 if (virtualMachine.listExtensions().values() instanceof List) {
								 vmVH.setExtensionList((List<VirtualMachineExtension>) virtualMachine.listExtensions().values());
							 } else {
								 Collection<VirtualMachineExtension> virtualMachineExtensionValue = virtualMachine.listExtensions().values();
								 vmVH.setExtensionList(new ArrayList<>(virtualMachineExtensionValue));
							 }
						 }
						 JsonObject properties = virtualMachineObject.getAsJsonObject("properties");
						 if (properties != null) {
							 HashMap<String, Object> propertiesMap = new Gson().fromJson(
									 properties.toString(),
									 HashMap.class);
							 vmVH.setProperties(propertiesMap);
						 }
						 vmList.add(vmVH);
				        }
			}catch(Exception e) {
				e.printStackTrace();
				log.error("Error Collecting info for {} {} ",virtualMachine.computerName(),e.getMessage());
			}
		}
		log.info("Target Type : {}  Total: {} ", "virtualmachine", vmList.size());
		return vmList;
	}

	private void setVnetInfo(VirtualMachine virtualMachine, VirtualMachineVH vmVH) {

		NicIPConfiguration ipConfiguration = virtualMachine.getPrimaryNetworkInterface().primaryIPConfiguration();

		vmVH.setVnet(ipConfiguration.networkId());
		vmVH.setVnetName(ipConfiguration.getNetwork().name());
		vmVH.setSubnet(ipConfiguration.subnetName());

	}

	private void setOtherVnets(VirtualMachine virtualMachine, VirtualMachineVH vmVH,
			List<NetworkInterface> networkInterfaces) {
		String primaryNetworkIntefaceId = virtualMachine.getPrimaryNetworkInterface().id();

		List<String> nicIds = virtualMachine.networkInterfaceIds();
		List<NetworkInterface> nics = networkInterfaces.stream()
				.filter(nic -> nicIds.contains(nic.id()) && !primaryNetworkIntefaceId.equals(nic.id()))
				.collect(Collectors.toList());
		List<Map<String, String>> vnetInfoList = new ArrayList<>();
		for (NetworkInterface nic : nics) {
			NicIPConfiguration ipConfiguration = nic.primaryIPConfiguration();
			String subnet = ipConfiguration.subnetName();
			String vnet = ipConfiguration.networkId();
			Map<String, String> vnetInfo = new HashMap<>();
			vnetInfo.put("vnet", vnet);
			vnetInfo.put("subnet", subnet);
			vnetInfoList.add(vnetInfo);
		}
		vmVH.setSecondaryNetworks(vnetInfoList);

	}

	private void setNsgs(VirtualMachine virtualMachine, VirtualMachineVH vmVH,
			List<NetworkInterface> networkInterfaces) {
		List<String> nicIds = virtualMachine.networkInterfaceIds();
		List<NetworkInterface> nics = networkInterfaces.stream().filter(nic -> nicIds.contains(nic.id()))
				.collect(Collectors.toList());

		List<Map<String, String>> nsgList = new ArrayList<>();
		String nsg;
		Map<String, String> nsgMap;
		for (NetworkInterface nic : nics) {
			NicIPConfiguration ipConfiguration = nic.primaryIPConfiguration();
			String subnet = ipConfiguration.subnetName();
			Optional<Subnet> subnetOptional = ipConfiguration.getNetwork().subnets().values().stream()
					.filter(subnetObj -> subnet.equals(subnetObj.name())).findAny();
			Subnet subnetObj = null;

			if (subnetOptional.isPresent()) {
				subnetObj = subnetOptional.get();
			}
			nsg = nic.networkSecurityGroupId();
			if (nsg != null) {
				nsgMap = new HashMap<>();
				nsgMap.put("nsg", nsg);
				nsgMap.put("attachedTo", nic.id());
				nsgMap.put("attachedToType", "nic");
				nsgMap.put("nicSubet", subnetObj.parent().id() + "/" + subnetObj.name());
				nsgList.add(nsgMap);
			}
			if (subnetObj != null) {
				nsg = subnetObj.networkSecurityGroupId();
				if (nsg != null) {
					nsgMap = new HashMap<>();
					nsgMap.put("nsg", nsg);
					nsgMap.put("attachedTo", subnetObj.parent().id() + "/" + subnetObj.name());
					nsgMap.put("attachedToType", "subnet");
					nsgList.add(nsgMap);
				}
			}
		}
		vmVH.setNetworkSecurityGroups(nsgList);

	}

	private void setVmDisks(VirtualMachine vm, VirtualMachineVH vmVH) {
		List<VMDiskVH> vmDisks = new ArrayList<>();
		OSDisk osDisk = vm.storageProfile().osDisk();
		VMDiskVH vmDisk = new VMDiskVH();
		vmDisk.setName(osDisk.name());
		vmDisk.setSizeInGB(osDisk.diskSizeGB());
		vmDisk.setCachingType(osDisk.caching().toString());
		try {
			vmDisk.setStorageAccountType(
					osDisk.managedDisk().storageAccountType() != null
							? osDisk.managedDisk().storageAccountType().toString()
							: "Unknown");
		} catch (Exception e) {
			vmDisk.setStorageAccountType("Unknown");
		}
		vmDisk.setType("OSDisk");

		if (osDisk.encryptionSettings() != null) {
			vmDisk.setisEncryptionEnabled(osDisk.encryptionSettings().enabled());
		}
		vmDisks.add(vmDisk);

		List<DataDisk> dataDisks = vm.storageProfile().dataDisks();
		for (DataDisk dataDisk : dataDisks) {
			vmDisk = new VMDiskVH();
			vmDisk.setName(dataDisk.name());
			vmDisk.setSizeInGB(dataDisk.diskSizeGB());
			try {
				vmDisk.setStorageAccountType(dataDisk.managedDisk().storageAccountType() != null
						? dataDisk.managedDisk().storageAccountType().toString()
						: "Unknown");
			} catch (Exception e) {
				vmDisk.setStorageAccountType("Unknown");
			}
			vmDisk.setCachingType(dataDisk.caching().toString());
			vmDisk.setType("DataDisk");
			vmDisks.add(vmDisk);
		}
		vmVH.setDisks(vmDisks);

	}

	@SuppressWarnings("unused")
	private String identifyPlatform(String os) {
		try {
			if (os.toLowerCase().contains("windows")) {
				return "windows";
			}
		} catch (Exception e) {

		}
		return "";
	}

}
