package com.tmobile.cloud.azurerules.virtualMachines;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.azurerules.VirtualMachine.EndpointProtectionRule;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.policies.CheckAzureSSHAuthenticationTypeRule;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.BaseRule;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class})

public class EndpointProtectionRuleTest {
    @InjectMocks
    EndpointProtectionRule endpointProtectionRule;


    public JsonObject getFailureJsonArrayForEndpointProtection(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\":\"2022-07-03 11:00:00+0000\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":\"centralus\",\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"DEV-PALADINCLOUD\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/DEV-PALADINCLOUD/providers/Microsoft.Compute/virtualMachines/testing\",\"computerName\":\"testing\",\"vmSize\":\"Standard_D2s_v3\",\"tags\":{},\"networkInterfaceIds\":[\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkInterfaces/testing243\"],\"osDiskStorageAccountType\":null,\"availabilityZones\":[],\"availabilitySetId\":null,\"provisioningState\":\"Succeeded\",\"licenseType\":null,\"disks\":[{\"storageAccountType\":\"Unknown\",\"name\":\"testing_disk1_82e2c9d0e28746c18f86e7dc52694290\",\"sizeInGB\":null,\"type\":\"OSDisk\",\"cachingType\":\"ReadWrite\",\"isEncryptionEnabled\":false}],\"vmId\":\"c992b2a8-8917-4d3d-950a-ef9ac6712ca7\",\"bootDiagnosticsStorageUri\":null,\"systemAssignedManagedServiceIdentityTenantId\":null,\"systemAssignedManagedServiceIdentityPrincipalId\":null,\"userAssignedManagedServiceIdentityIds\":[],\"name\":\"testing\",\"os\":null,\"osVersion\":null,\"privateIpAddress\":\"10.1.0.4\",\"publicIpAddress\":null,\"networkSecurityGroups\":[{\"nicSubet\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/virtualNetworks/dev-paladincloud-vnet/default\",\"nsg\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkSecurityGroups/testing-nsg\",\"attachedToType\":\"nic\",\"attachedTo\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkInterfaces/testing243\"}],\n                   \"extensionList\":[\n                      { \n                       \"reference\":false,\n                      \"instanceViewAsync\":{},\n                      \"instanceView\":null,\n                      \"name\":[]\n                      }\n                    ]\n                ,   \"vnet\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/virtualNetworks/dev-paladincloud-vnet\",\"subnet\":\"default\",\"vnetName\":\"dev-paladincloud-vnet\",\"primaryNCIMacAddress\":\"00-22-48-44-14-2A\",\"osType\":\"Linux\",\"primaryNetworkIntefaceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkInterfaces/testing243\",\"secondaryNetworks\":[],\"status\":\"deallocated\",\"managedDiskEnabled\":true,\"bootDiagnosticsEnabled\":true,\"managedServiceIdentityEnabled\":false,\"passwordBasedAuthenticationDisabled\":true\n           },\n        }\n    ]\n}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonArrayForEndpointProtection(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\":\"2022-07-03 11:00:00+0000\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":\"centralus\",\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"DEV-PALADINCLOUD\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/DEV-PALADINCLOUD/providers/Microsoft.Compute/virtualMachines/testing\",\"computerName\":\"testing\",\"vmSize\":\"Standard_D2s_v3\",\"tags\":{},\"networkInterfaceIds\":[\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkInterfaces/testing243\"],\"osDiskStorageAccountType\":null,\"availabilityZones\":[],\"availabilitySetId\":null,\"provisioningState\":\"Succeeded\",\"licenseType\":null,\"disks\":[{\"storageAccountType\":\"Unknown\",\"name\":\"testing_disk1_82e2c9d0e28746c18f86e7dc52694290\",\"sizeInGB\":null,\"type\":\"OSDisk\",\"cachingType\":\"ReadWrite\",\"isEncryptionEnabled\":false}],\"vmId\":\"c992b2a8-8917-4d3d-950a-ef9ac6712ca7\",\"bootDiagnosticsStorageUri\":null,\"systemAssignedManagedServiceIdentityTenantId\":null,\"systemAssignedManagedServiceIdentityPrincipalId\":null,\"userAssignedManagedServiceIdentityIds\":[],\"name\":\"testing\",\"os\":null,\"osVersion\":null,\"privateIpAddress\":\"10.1.0.4\",\"publicIpAddress\":null,\"networkSecurityGroups\":[{\"nicSubet\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/virtualNetworks/dev-paladincloud-vnet/default\",\"nsg\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkSecurityGroups/testing-nsg\",\"attachedToType\":\"nic\",\"attachedTo\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkInterfaces/testing243\"}],\n                   \"extensionList\":[\n                      { \n                       \"reference\":false,\n                      \"instanceViewAsync\":{},\n                      \"instanceView\":null,\n                      \"name\":[ \"EndpointSecurity\", \"TrendMicroDSA\", \"Antimalware\", \"EndpointProtection\", \"SCWPAgent\", \"PortalProtectExtension\",\"FileSecurity\"]\n                      }\n                    ]\n                ,   \"vnet\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/virtualNetworks/dev-paladincloud-vnet\",\"subnet\":\"default\",\"vnetName\":\"dev-paladincloud-vnet\",\"primaryNCIMacAddress\":\"00-22-48-44-14-2A\",\"osType\":\"Linux\",\"primaryNetworkIntefaceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkInterfaces/testing243\",\"secondaryNetworks\":[],\"status\":\"deallocated\",\"managedDiskEnabled\":true,\"bootDiagnosticsEnabled\":true,\"managedServiceIdentityEnabled\":false,\"passwordBasedAuthenticationDisabled\":true\n           },\n        }\n    ]\n}", JsonElement.class));
        return jsonObject;
    }
    @Test
    public void executeSucessTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForEndpointProtection());
        assertThat(endpointProtectionRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeFailureTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForEndpointProtection());
        assertThat(endpointProtectionRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(endpointProtectionRule.getHelpText(), is(notNullValue()));
    }

}