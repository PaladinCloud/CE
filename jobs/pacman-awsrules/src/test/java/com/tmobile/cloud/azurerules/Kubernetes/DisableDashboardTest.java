package com.tmobile.cloud.azurerules.Kubernetes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.BaseRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class})
public class DisableDashboardTest {

    @InjectMocks
    DisableDashboard disableDashboard;

    @Test
    public void executeSucessTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForDefineAuthorisedIPRanges());
        assertThat(disableDashboard.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    private JsonObject getHitJsonArrayForDefineAuthorisedIPRanges() {
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\":[{\"_source\":{\"discoverydate\":\"2022-11-2413:00:00+0530\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":\"eastus\",\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"dev-paladincloud\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\"enableRBAC\":true,\"properties\":{\"agentPoolProfiles\":[{\"name\":\"agentpool\",\"count\":1,\"vmSize\":\"Standard_DS2_v2\",\"osDiskSizeGB\":128,\"osDiskType\":\"Managed\",\"kubeletDiskType\":\"OS\",\"maxPods\":110,\"type\":\"VirtualMachineScaleSets\",\"availabilityZones\":[\"1\",\"2\",\"3\"],\"maxCount\":1,\"minCount\":1,\"enableAutoScaling\":true,\"provisioningState\":\"Succeeded\",\"powerState\":{\"code\":\"Running\"},\"orchestratorVersion\":\"1.23.12\",\"currentOrchestratorVersion\":\"1.23.12\",\"enableNodePublicIP\":false,\"mode\":\"System\",\"osType\":\"Linux\",\"osSKU\":\"Ubuntu\",\"nodeImageVersion\":\"AKSUbuntu-1804gen2containerd-2022.10.12\",\"enableFIPS\":false},{\"name\":\"test\",\"count\":1,\"vmSize\":\"Standard_D2s_v3\",\"osDiskSizeGB\":128,\"osDiskType\":\"Managed\",\"kubeletDiskType\":\"OS\",\"maxPods\":110,\"type\":\"VirtualMachineScaleSets\",\"availabilityZones\":[\"1\",\"2\",\"3\"],\"maxCount\":12,\"minCount\":1,\"enableAutoScaling\":true,\"provisioningState\":\"Succeeded\",\"powerState\":{\"code\":\"Running\"},\"orchestratorVersion\":\"1.23.12\",\"currentOrchestratorVersion\":\"1.23.12\",\"enableNodePublicIP\":false,\"mode\":\"User\",\"osType\":\"Linux\",\"osSKU\":\"Ubuntu\",\"nodeImageVersion\":\"AKSUbuntu-1804gen2containerd-2022.10.12\",\"enableFIPS\":false}],\"enableRBAC\":true,\"fqdn\":\"testrbac-dns-5e652cca.hcp.eastus.azmk8s.io\",\"servicePrincipalProfile\":{\"clientId\":\"msi\"},\"provisioningState\":\"Succeeded\",\"nodeResourceGroup\":\"MC_dev-paladincloud_testRBAC_eastus\",\"azurePortalFQDN\":\"testrbac-dns-5e652cca.portal.hcp.eastus.azmk8s.io\",\"autoScalerProfile\":{\"balance-similar-node-groups\":\"false\",\"expander\":\"random\",\"max-empty-bulk-delete\":\"10\",\"max-graceful-termination-sec\":\"600\",\"max-node-provision-time\":\"15m\",\"max-total-unready-percentage\":\"45\",\"new-pod-scale-up-delay\":\"0s\",\"ok-total-unready-count\":\"3\",\"scale-down-delay-after-add\":\"10m\",\"scale-down-delay-after-delete\":\"10s\",\"scale-down-delay-after-failure\":\"3m\",\"scale-down-unneeded-time\":\"10m\",\"scale-down-unready-time\":\"20m\",\"scale-down-utilization-threshold\":\"0.5\",\"scan-interval\":\"10s\",\"skip-nodes-with-local-storage\":\"false\",\"skip-nodes-with-system-pods\":\"true\"},\"oidcIssuerProfile\":{\"enabled\":false},\"currentKubernetesVersion\":\"1.23.12\",\"powerState\":{\"code\":\"Running\"},\"identityProfile\":{\"kubeletidentity\":{\"resourceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testRBAC-agentpool\",\"clientId\":\"734318d1-01dd-4c2e-8655-679f7e09ccee\",\"objectId\":\"94db99a7-241a-444f-a0a1-a6296b78c09e\"}},\"securityProfile\":{\"defender\":{\"logAnalyticsWorkspaceResourceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/DefaultResourceGroup-EUS/providers/Microsoft.OperationalInsights/workspaces/DefaultWorkspace-f4d319d8-7eac-4e15-a561-400f7744aa81-EUS\",\"securityMonitoring\":{\"enabled\":true}}},\"addonProfiles\":{\"azureKeyvaultSecretsProvider\":{\"enabled\":false,\"config\":null},\"azurepolicy\":{\"enabled\":true,\"config\":null,\"identity\":{\"resourceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/azurepolicy-testrbac\",\"clientId\":\"3f7adaf9-1a1a-4cb1-90d2-f02b4d95e4d1\",\"objectId\":\"b78f888e-f3d3-4525-9c56-de891de27bde\"}},\"httpApplicationRouting\":{\"enabled\":false,\"config\":null},\"omsAgent\":{\"enabled\":true,\"config\":{\"logAnalyticsWorkspaceResourceID\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/defaultresourcegroup-cus/providers/microsoft.operationalinsights/workspaces/defaultworkspace-f4d319d8-7eac-4e15-a561-400f7744aa81-cus\"},\"identity\":{\"resourceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/omsagent-testrbac\",\"clientId\":\"03d7c3ec-301b-4915-bfb4-0e0adefa24d8\",\"objectId\":\"484f5318-fbbc-4bc7-8570-7407ebbd74c0\"}}},\"storageProfile\":{\"diskCSIDriver\":{\"enabled\":true},\"fileCSIDriver\":{\"enabled\":true},\"snapshotController\":{\"enabled\":true}},\"dnsPrefix\":\"testRBAC-dns\",\"networkProfile\":{\"networkPlugin\":\"kubenet\",\"loadBalancerSku\":\"Standard\",\"loadBalancerProfile\":{\"managedOutboundIPs\":{\"count\":1},\"effectiveOutboundIPs\":[{\"id\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.Network/publicIPAddresses/cf2fb358-0f8f-446a-9316-74040997cdf1\"}]},\"podCidr\":\"10.244.0.0/16\",\"serviceCidr\":\"10.0.0.0/16\",\"dnsServiceIP\":\"10.0.0.10\",\"dockerBridgeCidr\":\"172.17.0.1/16\",\"outboundType\":\"loadBalancer\",\"podCidrs\":[\"10.244.0.0/16\"],\"serviceCidrs\":[\"10.0.0.0/16\"],\"ipFamilies\":[\"IPv4\"]},\"disableLocalAccounts\":false,\"maxAgentPools\":100,\"kubernetesVersion\":\"1.23.12\"},\"dashBoardEnabled\":false,\"_resourceid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\"_docid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\"_entity\":\"true\",\"_entitytype\":\"kubernetes\",\"firstdiscoveredon\":\"2022-11-2413:00:00+0530\",\"latest\":true,\"_loaddate\":\"2022-11-2507:00:00+0000\"}}]}", JsonElement.class));
        return jsonObject;
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForDefineAuthorisedIpRanges());
        assertThat(disableDashboard.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private JsonObject getFailureJsonArrayForDefineAuthorisedIpRanges() {
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\":[{\"_source\":{\"discoverydate\":\"2022-11-2413:00:00+0530\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":\"eastus\",\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"dev-paladincloud\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\"enableRBAC\":true,\"properties\":{\"agentPoolProfiles\":[{\"name\":\"agentpool\",\"count\":1,\"vmSize\":\"Standard_DS2_v2\",\"osDiskSizeGB\":128,\"osDiskType\":\"Managed\",\"kubeletDiskType\":\"OS\",\"maxPods\":110,\"type\":\"VirtualMachineScaleSets\",\"availabilityZones\":[\"1\",\"2\",\"3\"],\"maxCount\":1,\"minCount\":1,\"enableAutoScaling\":true,\"provisioningState\":\"Succeeded\",\"powerState\":{\"code\":\"Running\"},\"orchestratorVersion\":\"1.23.12\",\"currentOrchestratorVersion\":\"1.23.12\",\"enableNodePublicIP\":false,\"mode\":\"System\",\"osType\":\"Linux\",\"osSKU\":\"Ubuntu\",\"nodeImageVersion\":\"AKSUbuntu-1804gen2containerd-2022.10.12\",\"enableFIPS\":false},{\"name\":\"test\",\"count\":1,\"vmSize\":\"Standard_D2s_v3\",\"osDiskSizeGB\":128,\"osDiskType\":\"Managed\",\"kubeletDiskType\":\"OS\",\"maxPods\":110,\"type\":\"VirtualMachineScaleSets\",\"availabilityZones\":[\"1\",\"2\",\"3\"],\"maxCount\":12,\"minCount\":1,\"enableAutoScaling\":true,\"provisioningState\":\"Succeeded\",\"powerState\":{\"code\":\"Running\"},\"orchestratorVersion\":\"1.23.12\",\"currentOrchestratorVersion\":\"1.23.12\",\"enableNodePublicIP\":false,\"mode\":\"User\",\"osType\":\"Linux\",\"osSKU\":\"Ubuntu\",\"nodeImageVersion\":\"AKSUbuntu-1804gen2containerd-2022.10.12\",\"enableFIPS\":false}],\"enableRBAC\":true,\"fqdn\":\"testrbac-dns-5e652cca.hcp.eastus.azmk8s.io\",\"servicePrincipalProfile\":{\"clientId\":\"msi\"},\"provisioningState\":\"Succeeded\",\"nodeResourceGroup\":\"MC_dev-paladincloud_testRBAC_eastus\",\"azurePortalFQDN\":\"testrbac-dns-5e652cca.portal.hcp.eastus.azmk8s.io\",\"autoScalerProfile\":{\"balance-similar-node-groups\":\"false\",\"expander\":\"random\",\"max-empty-bulk-delete\":\"10\",\"max-graceful-termination-sec\":\"600\",\"max-node-provision-time\":\"15m\",\"max-total-unready-percentage\":\"45\",\"new-pod-scale-up-delay\":\"0s\",\"ok-total-unready-count\":\"3\",\"scale-down-delay-after-add\":\"10m\",\"scale-down-delay-after-delete\":\"10s\",\"scale-down-delay-after-failure\":\"3m\",\"scale-down-unneeded-time\":\"10m\",\"scale-down-unready-time\":\"20m\",\"scale-down-utilization-threshold\":\"0.5\",\"scan-interval\":\"10s\",\"skip-nodes-with-local-storage\":\"false\",\"skip-nodes-with-system-pods\":\"true\"},\"oidcIssuerProfile\":{\"enabled\":false},\"currentKubernetesVersion\":\"1.23.12\",\"powerState\":{\"code\":\"Running\"},\"identityProfile\":{\"kubeletidentity\":{\"resourceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testRBAC-agentpool\",\"clientId\":\"734318d1-01dd-4c2e-8655-679f7e09ccee\",\"objectId\":\"94db99a7-241a-444f-a0a1-a6296b78c09e\"}},\"securityProfile\":{\"defender\":{\"logAnalyticsWorkspaceResourceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/DefaultResourceGroup-EUS/providers/Microsoft.OperationalInsights/workspaces/DefaultWorkspace-f4d319d8-7eac-4e15-a561-400f7744aa81-EUS\",\"securityMonitoring\":{\"enabled\":true}}},\"addonProfiles\":{\"azureKeyvaultSecretsProvider\":{\"enabled\":false,\"config\":null},\"azurepolicy\":{\"enabled\":true,\"config\":null,\"identity\":{\"resourceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/azurepolicy-testrbac\",\"clientId\":\"3f7adaf9-1a1a-4cb1-90d2-f02b4d95e4d1\",\"objectId\":\"b78f888e-f3d3-4525-9c56-de891de27bde\"}},\"httpApplicationRouting\":{\"enabled\":false,\"config\":null},\"omsAgent\":{\"enabled\":true,\"config\":{\"logAnalyticsWorkspaceResourceID\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/defaultresourcegroup-cus/providers/microsoft.operationalinsights/workspaces/defaultworkspace-f4d319d8-7eac-4e15-a561-400f7744aa81-cus\"},\"identity\":{\"resourceId\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/omsagent-testrbac\",\"clientId\":\"03d7c3ec-301b-4915-bfb4-0e0adefa24d8\",\"objectId\":\"484f5318-fbbc-4bc7-8570-7407ebbd74c0\"}}},\"storageProfile\":{\"diskCSIDriver\":{\"enabled\":true},\"fileCSIDriver\":{\"enabled\":true},\"snapshotController\":{\"enabled\":true}},\"dnsPrefix\":\"testRBAC-dns\",\"networkProfile\":{\"networkPlugin\":\"kubenet\",\"loadBalancerSku\":\"Standard\",\"loadBalancerProfile\":{\"managedOutboundIPs\":{\"count\":1},\"effectiveOutboundIPs\":[{\"id\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.Network/publicIPAddresses/cf2fb358-0f8f-446a-9316-74040997cdf1\"}]},\"podCidr\":\"10.244.0.0/16\",\"serviceCidr\":\"10.0.0.0/16\",\"dnsServiceIP\":\"10.0.0.10\",\"dockerBridgeCidr\":\"172.17.0.1/16\",\"outboundType\":\"loadBalancer\",\"podCidrs\":[\"10.244.0.0/16\"],\"serviceCidrs\":[\"10.0.0.0/16\"],\"ipFamilies\":[\"IPv4\"]},\"disableLocalAccounts\":true,\"maxAgentPools\":100,\"kubernetesVersion\":\"1.23.12\"},\"dashBoardEnabled\":true,\"_resourceid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\"_docid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\"_entity\":\"true\",\"_entitytype\":\"kubernetes\",\"firstdiscoveredon\":\"2022-11-2413:00:00+0530\",\"latest\":true,\"_loaddate\":\"2022-11-2507:00:00+0000\"}}]}", JsonElement.class));
        return jsonObject;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(disableDashboard.getHelpText(), is(notNullValue()));
    }
}
