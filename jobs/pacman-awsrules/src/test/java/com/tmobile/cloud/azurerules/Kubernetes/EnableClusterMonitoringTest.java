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
public class EnableClusterMonitoringTest {
//    @InjectMocks
//    EnableClusterMonitoring enableClusterMonitoring;
//    @Test
//    public void executeSucessTest() throws Exception {
//        mockStatic(PacmanUtils.class);
//        mockStatic(RulesElasticSearchRepositoryUtil.class);
//        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
//                .thenReturn(
//                        true);
//        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
//                anyObject(),
//                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForEnableClusterMonitoring());
//        assertThat(enableClusterMonitoring.execute(CommonTestUtils.getMapString("r_123 "),
//                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
//    }
//
//    private JsonObject getHitJsonArrayForEnableClusterMonitoring() {
//        Gson gson=new Gson();
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n   {\n" +
//                "        \"_index\": \"azure_kubernetes\",\n" +
//                "        \"_type\": \"kubernetes\",\n" +
//                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\n" +
//                "        \"_score\": 0.18232156,\n" +
//                "        \"_source\": {\n" +
//                "          \"discoverydate\": \"2022-11-22 16:00:00+0530\",\n" +
//                "          \"_cloudType\": \"Azure\",\n" +
//                "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
//                "          \"region\": \"eastus\",\n" +
//                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
//                "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
//                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\n" +
//                "          \"enableRBAC\": true,\n" +
//                "          \"properties\": {\n" +
//                "            \"agentPoolProfiles\": [\n" +
//                "              {\n" +
//                "                \"name\": \"agentpool\",\n" +
//                "                \"count\": 1,\n" +
//                "                \"vmSize\": \"Standard_DS2_v2\",\n" +
//                "                \"osDiskSizeGB\": 128,\n" +
//                "                \"osDiskType\": \"Managed\",\n" +
//                "                \"kubeletDiskType\": \"OS\",\n" +
//                "                \"maxPods\": 110,\n" +
//                "                \"type\": \"VirtualMachineScaleSets\",\n" +
//                "                \"availabilityZones\": [\n" +
//                "                  \"1\",\n" +
//                "                  \"2\",\n" +
//                "                  \"3\"\n" +
//                "                ],\n" +
//                "                \"maxCount\": 1,\n" +
//                "                \"minCount\": 1,\n" +
//                "                \"enableAutoScaling\": true,\n" +
//                "                \"provisioningState\": \"Succeeded\",\n" +
//                "                \"powerState\": {\n" +
//                "                  \"code\": \"Running\"\n" +
//                "                },\n" +
//                "                \"orchestratorVersion\": \"1.23.12\",\n" +
//                "                \"currentOrchestratorVersion\": \"1.23.12\",\n" +
//                "                \"enableNodePublicIP\": false,\n" +
//                "                \"mode\": \"System\",\n" +
//                "                \"osType\": \"Linux\",\n" +
//                "                \"osSKU\": \"Ubuntu\",\n" +
//                "                \"nodeImageVersion\": \"AKSUbuntu-1804gen2containerd-2022.10.12\",\n" +
//                "                \"enableFIPS\": false\n" +
//                "              },\n" +
//                "              {\n" +
//                "                \"name\": \"test\",\n" +
//                "                \"count\": 1,\n" +
//                "                \"vmSize\": \"Standard_D2s_v3\",\n" +
//                "                \"osDiskSizeGB\": 128,\n" +
//                "                \"osDiskType\": \"Managed\",\n" +
//                "                \"kubeletDiskType\": \"OS\",\n" +
//                "                \"maxPods\": 110,\n" +
//                "                \"type\": \"VirtualMachineScaleSets\",\n" +
//                "                \"availabilityZones\": [\n" +
//                "                  \"1\",\n" +
//                "                  \"2\",\n" +
//                "                  \"3\"\n" +
//                "                ],\n" +
//                "                \"maxCount\": 12,\n" +
//                "                \"minCount\": 1,\n" +
//                "                \"enableAutoScaling\": true,\n" +
//                "                \"provisioningState\": \"Succeeded\",\n" +
//                "                \"powerState\": {\n" +
//                "                  \"code\": \"Running\"\n" +
//                "                },\n" +
//                "                \"orchestratorVersion\": \"1.23.12\",\n" +
//                "                \"currentOrchestratorVersion\": \"1.23.12\",\n" +
//                "                \"enableNodePublicIP\": false,\n" +
//                "                \"mode\": \"User\",\n" +
//                "                \"osType\": \"Linux\",\n" +
//                "                \"osSKU\": \"Ubuntu\",\n" +
//                "                \"nodeImageVersion\": \"AKSUbuntu-1804gen2containerd-2022.10.12\",\n" +
//                "                \"enableFIPS\": false\n" +
//                "              }\n" +
//                "            ],\n" +
//                "            \"enableRBAC\": true,\n" +
//                "            \"fqdn\": \"testrbac-dns-5e652cca.hcp.eastus.azmk8s.io\",\n" +
//                "            \"servicePrincipalProfile\": {\n" +
//                "              \"clientId\": \"msi\"\n" +
//                "            },\n" +
//                "            \"provisioningState\": \"Succeeded\",\n" +
//                "            \"nodeResourceGroup\": \"MC_dev-paladincloud_testRBAC_eastus\",\n" +
//                "            \"azurePortalFQDN\": \"testrbac-dns-5e652cca.portal.hcp.eastus.azmk8s.io\",\n" +
//                "            \"autoScalerProfile\": {\n" +
//                "              \"balance-similar-node-groups\": \"false\",\n" +
//                "              \"expander\": \"random\",\n" +
//                "              \"max-empty-bulk-delete\": \"10\",\n" +
//                "              \"max-graceful-termination-sec\": \"600\",\n" +
//                "              \"max-node-provision-time\": \"15m\",\n" +
//                "              \"max-total-unready-percentage\": \"45\",\n" +
//                "              \"new-pod-scale-up-delay\": \"0s\",\n" +
//                "              \"ok-total-unready-count\": \"3\",\n" +
//                "              \"scale-down-delay-after-add\": \"10m\",\n" +
//                "              \"scale-down-delay-after-delete\": \"10s\",\n" +
//                "              \"scale-down-delay-after-failure\": \"3m\",\n" +
//                "              \"scale-down-unneeded-time\": \"10m\",\n" +
//                "              \"scale-down-unready-time\": \"20m\",\n" +
//                "              \"scale-down-utilization-threshold\": \"0.5\",\n" +
//                "              \"scan-interval\": \"10s\",\n" +
//                "              \"skip-nodes-with-local-storage\": \"false\",\n" +
//                "              \"skip-nodes-with-system-pods\": \"true\"\n" +
//                "            },\n" +
//                "            \"oidcIssuerProfile\": {\n" +
//                "              \"enabled\": false\n" +
//                "            },\n" +
//                "            \"currentKubernetesVersion\": \"1.23.12\",\n" +
//                "            \"powerState\": {\n" +
//                "              \"code\": \"Running\"\n" +
//                "            },\n" +
//                "            \"identityProfile\": {\n" +
//                "              \"kubeletidentity\": {\n" +
//                "                \"resourceId\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testRBAC-agentpool\",\n" +
//                "                \"clientId\": \"734318d1-01dd-4c2e-8655-679f7e09ccee\",\n" +
//                "                \"objectId\": \"94db99a7-241a-444f-a0a1-a6296b78c09e\"\n" +
//                "              }\n" +
//                "            },\n" +
//                "            \"securityProfile\": {\n" +
//                "              \"defender\": {\n" +
//                "                \"logAnalyticsWorkspaceResourceId\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/DefaultResourceGroup-EUS/providers/Microsoft.OperationalInsights/workspaces/DefaultWorkspace-f4d319d8-7eac-4e15-a561-400f7744aa81-EUS\",\n" +
//                "                \"securityMonitoring\": {\n" +
//                "                  \"enabled\": true\n" +
//                "                }\n" +
//                "              }\n" +
//                "            },\n" +
//                "            \"addonProfiles\": {\n" +
//                "              \"azureKeyvaultSecretsProvider\": {\n" +
//                "                \"enabled\": false,\n" +
//                "                \"config\": null\n" +
//                "              },\n" +
//                "              \"azurepolicy\": {\n" +
//                "                \"enabled\": true,\n" +
//                "                \"config\": null,\n" +
//                "                \"identity\": {\n" +
//                "                  \"resourceId\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/azurepolicy-testrbac\",\n" +
//                "                  \"clientId\": \"3f7adaf9-1a1a-4cb1-90d2-f02b4d95e4d1\",\n" +
//                "                  \"objectId\": \"b78f888e-f3d3-4525-9c56-de891de27bde\"\n" +
//                "                }\n" +
//                "              },\n" +
//                "              \"httpApplicationRouting\": {\n" +
//                "                \"enabled\": false,\n" +
//                "                \"config\": null\n" +
//                "              },\n" +
//                "              \"omsAgent\": {\n" +
//                "                \"enabled\": true,\n" +
//                "                \"config\": {\n" +
//                "                  \"logAnalyticsWorkspaceResourceID\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/defaultresourcegroup-cus/providers/microsoft.operationalinsights/workspaces/defaultworkspace-f4d319d8-7eac-4e15-a561-400f7744aa81-cus\"\n" +
//                "                },\n" +
//                "                \"identity\": {\n" +
//                "                  \"resourceId\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/omsagent-testrbac\",\n" +
//                "                  \"clientId\": \"03d7c3ec-301b-4915-bfb4-0e0adefa24d8\",\n" +
//                "                  \"objectId\": \"484f5318-fbbc-4bc7-8570-7407ebbd74c0\"\n" +
//                "                }\n" +
//                "              }\n" +
//                "            },\n" +
//                "            \"storageProfile\": {\n" +
//                "              \"diskCSIDriver\": {\n" +
//                "                \"enabled\": true\n" +
//                "              },\n" +
//                "              \"fileCSIDriver\": {\n" +
//                "                \"enabled\": true\n" +
//                "              },\n" +
//                "              \"snapshotController\": {\n" +
//                "                \"enabled\": true\n" +
//                "              }\n" +
//                "            },\n" +
//                "            \"dnsPrefix\": \"testRBAC-dns\",\n" +
//                "            \"networkProfile\": {\n" +
//                "              \"networkPlugin\": \"kubenet\",\n" +
//                "              \"loadBalancerSku\": \"Standard\",\n" +
//                "              \"loadBalancerProfile\": {\n" +
//                "                \"managedOutboundIPs\": {\n" +
//                "                  \"count\": 1\n" +
//                "                },\n" +
//                "                \"effectiveOutboundIPs\": [\n" +
//                "                  {\n" +
//                "                    \"id\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_testRBAC_eastus/providers/Microsoft.Network/publicIPAddresses/cf2fb358-0f8f-446a-9316-74040997cdf1\"\n" +
//                "                  }\n" +
//                "                ]\n" +
//                "              },\n" +
//                "              \"podCidr\": \"10.244.0.0/16\",\n" +
//                "              \"serviceCidr\": \"10.0.0.0/16\",\n" +
//                "              \"dnsServiceIP\": \"10.0.0.10\",\n" +
//                "              \"dockerBridgeCidr\": \"172.17.0.1/16\",\n" +
//                "              \"outboundType\": \"loadBalancer\",\n" +
//                "              \"podCidrs\": [\n" +
//                "                \"10.244.0.0/16\"\n" +
//                "              ],\n" +
//                "              \"serviceCidrs\": [\n" +
//                "                \"10.0.0.0/16\"\n" +
//                "              ],\n" +
//                "              \"ipFamilies\": [\n" +
//                "                \"IPv4\"\n" +
//                "              ]\n" +
//                "            },\n" +
//                "            \"disableLocalAccounts\": false,\n" +
//                "            \"maxAgentPools\": 100,\n" +
//                "            \"kubernetesVersion\": \"1.23.12\"\n" +
//                "          },\n" +
//                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\n" +
//                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/testRBAC\",\n" +
//                "          \"_entity\": \"true\",\n" +
//                "          \"_entitytype\": \"kubernetes\",\n" +
//                "          \"firstdiscoveredon\": \"2022-11-22 16:00:00+0530\",\n" +
//                "          \"latest\": true,\n" +
//                "          \"_loaddate\": \"2022-11-23 11:30:00+0000\"\n" +
//                "        }\n" +
//                "      } \n]\n}", JsonElement.class));
//        return jsonObject;
//    }
//
//    @Test
//    public void executeFailureTest() throws Exception {
//        mockStatic(PacmanUtils.class);
//        mockStatic(RulesElasticSearchRepositoryUtil.class);
//        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
//                .thenReturn(
//                        true);
//        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
//                anyObject(),
//                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForEnableClusterMonitoring());
//        assertThat(enableClusterMonitoring.execute(CommonTestUtils.getMapString("r_123 "),
//                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
//    }
//
//    private JsonObject getFailureJsonArrayForEnableClusterMonitoring() {
//        Gson gson=new Gson();
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n {\n" +
//                "        \"_index\": \"azure_kubernetes\",\n" +
//                "        \"_type\": \"kubernetes\",\n" +
//                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/unmonitored\",\n" +
//                "        \"_score\": 0.18232156,\n" +
//                "        \"_source\": {\n" +
//                "          \"discoverydate\": \"2022-11-22 16:00:00+0530\",\n" +
//                "          \"_cloudType\": \"Azure\",\n" +
//                "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
//                "          \"region\": \"eastus\",\n" +
//                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
//                "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
//                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/unmonitored\",\n" +
//                "          \"enableRBAC\": true,\n" +
//                "          \"properties\": {\n" +
//                "            \"agentPoolProfiles\": [\n" +
//                "              {\n" +
//                "                \"name\": \"agentpool\",\n" +
//                "                \"count\": 1,\n" +
//                "                \"vmSize\": \"Standard_DS2_v2\",\n" +
//                "                \"osDiskSizeGB\": 128,\n" +
//                "                \"osDiskType\": \"Managed\",\n" +
//                "                \"kubeletDiskType\": \"OS\",\n" +
//                "                \"maxPods\": 110,\n" +
//                "                \"type\": \"VirtualMachineScaleSets\",\n" +
//                "                \"availabilityZones\": [\n" +
//                "                  \"1\",\n" +
//                "                  \"2\",\n" +
//                "                  \"3\"\n" +
//                "                ],\n" +
//                "                \"maxCount\": 5,\n" +
//                "                \"minCount\": 1,\n" +
//                "                \"enableAutoScaling\": true,\n" +
//                "                \"provisioningState\": \"Succeeded\",\n" +
//                "                \"powerState\": {\n" +
//                "                  \"code\": \"Running\"\n" +
//                "                },\n" +
//                "                \"orchestratorVersion\": \"1.23.12\",\n" +
//                "                \"currentOrchestratorVersion\": \"1.23.12\",\n" +
//                "                \"enableNodePublicIP\": false,\n" +
//                "                \"mode\": \"System\",\n" +
//                "                \"osType\": \"Linux\",\n" +
//                "                \"osSKU\": \"Ubuntu\",\n" +
//                "                \"nodeImageVersion\": \"AKSUbuntu-1804gen2containerd-2022.10.24\",\n" +
//                "                \"enableFIPS\": false\n" +
//                "              }\n" +
//                "            ],\n" +
//                "            \"enableRBAC\": true,\n" +
//                "            \"fqdn\": \"unmonitored-dns-82703afc.hcp.eastus.azmk8s.io\",\n" +
//                "            \"servicePrincipalProfile\": {\n" +
//                "              \"clientId\": \"msi\"\n" +
//                "            },\n" +
//                "            \"provisioningState\": \"Succeeded\",\n" +
//                "            \"nodeResourceGroup\": \"MC_dev-paladincloud_unmonitored_eastus\",\n" +
//                "            \"azurePortalFQDN\": \"unmonitored-dns-82703afc.portal.hcp.eastus.azmk8s.io\",\n" +
//                "            \"autoScalerProfile\": {\n" +
//                "              \"balance-similar-node-groups\": \"false\",\n" +
//                "              \"expander\": \"random\",\n" +
//                "              \"max-empty-bulk-delete\": \"10\",\n" +
//                "              \"max-graceful-termination-sec\": \"600\",\n" +
//                "              \"max-node-provision-time\": \"15m\",\n" +
//                "              \"max-total-unready-percentage\": \"45\",\n" +
//                "              \"new-pod-scale-up-delay\": \"0s\",\n" +
//                "              \"ok-total-unready-count\": \"3\",\n" +
//                "              \"scale-down-delay-after-add\": \"10m\",\n" +
//                "              \"scale-down-delay-after-delete\": \"10s\",\n" +
//                "              \"scale-down-delay-after-failure\": \"3m\",\n" +
//                "              \"scale-down-unneeded-time\": \"10m\",\n" +
//                "              \"scale-down-unready-time\": \"20m\",\n" +
//                "              \"scale-down-utilization-threshold\": \"0.5\",\n" +
//                "              \"scan-interval\": \"10s\",\n" +
//                "              \"skip-nodes-with-local-storage\": \"false\",\n" +
//                "              \"skip-nodes-with-system-pods\": \"true\"\n" +
//                "            },\n" +
//                "            \"oidcIssuerProfile\": {\n" +
//                "              \"enabled\": false\n" +
//                "            },\n" +
//                "            \"currentKubernetesVersion\": \"1.23.12\",\n" +
//                "            \"powerState\": {\n" +
//                "              \"code\": \"Running\"\n" +
//                "            },\n" +
//                "            \"identityProfile\": {\n" +
//                "              \"kubeletidentity\": {\n" +
//                "                \"resourceId\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_unmonitored_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/unmonitored-agentpool\",\n" +
//                "                \"clientId\": \"553fd7b2-ddf5-4c43-9e97-21c2c987e2c6\",\n" +
//                "                \"objectId\": \"27371bec-04f7-4ede-846b-b05e9095bc48\"\n" +
//                "              }\n" +
//                "            },\n" +
//                "            \"securityProfile\": {\n" +
//                "              \"defender\": {\n" +
//                "                \"logAnalyticsWorkspaceResourceId\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/DefaultResourceGroup-EUS/providers/Microsoft.OperationalInsights/workspaces/DefaultWorkspace-f4d319d8-7eac-4e15-a561-400f7744aa81-EUS\",\n" +
//                "                \"securityMonitoring\": {\n" +
//                "                  \"enabled\": true\n" +
//                "                }\n" +
//                "              }\n" +
//                "            },\n" +
//                "            \"addonProfiles\": {\n" +
//                "              \"azureKeyvaultSecretsProvider\": {\n" +
//                "                \"enabled\": false,\n" +
//                "                \"config\": null\n" +
//                "              },\n" +
//                "              \"azurepolicy\": {\n" +
//                "                \"enabled\": true,\n" +
//                "                \"config\": null,\n" +
//                "                \"identity\": {\n" +
//                "                  \"resourceId\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/MC_dev-paladincloud_unmonitored_eastus/providers/Microsoft.ManagedIdentity/userAssignedIdentities/azurepolicy-unmonitored\",\n" +
//                "                  \"clientId\": \"3ec501a2-fb57-4741-9249-5dc59cf443d2\",\n" +
//                "                  \"objectId\": \"2187f5d7-b36d-4a6d-87f9-6f88e2731eb6\"\n" +
//                "                }\n" +
//                "              },\n" +
//                "              \"httpApplicationRouting\": {\n" +
//                "                \"enabled\": false,\n" +
//                "                \"config\": null\n" +
//                "              }\n" +
//                "            },\n" +
//                "            \"storageProfile\": {\n" +
//                "              \"diskCSIDriver\": {\n" +
//                "                \"enabled\": true\n" +
//                "              },\n" +
//                "              \"fileCSIDriver\": {\n" +
//                "                \"enabled\": true\n" +
//                "              },\n" +
//                "              \"snapshotController\": {\n" +
//                "                \"enabled\": true\n" +
//                "              }\n" +
//                "            },\n" +
//                "            \"dnsPrefix\": \"unmonitored-dns\",\n" +
//                "            \"networkProfile\": {\n" +
//                "              \"networkPlugin\": \"kubenet\",\n" +
//                "              \"loadBalancerSku\": \"Standard\",\n" +
//                "              \"loadBalancerProfile\": {\n" +
//                "                \"managedOutboundIPs\": {\n" +
//                "                  \"count\": 1\n" +
//                "                },\n" +
//                "                \"effectiveOutboundIPs\": [\n" +
//                "                  {\n" +
//                "                    \"id\": \"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_unmonitored_eastus/providers/Microsoft.Network/publicIPAddresses/508dcef0-3e35-435b-beb1-68ce506ec0fa\"\n" +
//                "                  }\n" +
//                "                ]\n" +
//                "              },\n" +
//                "              \"podCidr\": \"10.244.0.0/16\",\n" +
//                "              \"serviceCidr\": \"10.0.0.0/16\",\n" +
//                "              \"dnsServiceIP\": \"10.0.0.10\",\n" +
//                "              \"dockerBridgeCidr\": \"172.17.0.1/16\",\n" +
//                "              \"outboundType\": \"loadBalancer\",\n" +
//                "              \"podCidrs\": [\n" +
//                "                \"10.244.0.0/16\"\n" +
//                "              ],\n" +
//                "              \"serviceCidrs\": [\n" +
//                "                \"10.0.0.0/16\"\n" +
//                "              ],\n" +
//                "              \"ipFamilies\": [\n" +
//                "                \"IPv4\"\n" +
//                "              ]\n" +
//                "            },\n" +
//                "            \"disableLocalAccounts\": false,\n" +
//                "            \"maxAgentPools\": 100,\n" +
//                "            \"kubernetesVersion\": \"1.23.12\"\n" +
//                "          },\n" +
//                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/unmonitored\",\n" +
//                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/unmonitored\",\n" +
//                "          \"_entity\": \"true\",\n" +
//                "          \"_entitytype\": \"kubernetes\",\n" +
//                "          \"firstdiscoveredon\": \"2022-11-22 16:00:00+0530\",\n" +
//                "          \"latest\": true,\n" +
//                "          \"_loaddate\": \"2022-11-23 11:30:00+0000\"\n" +
//                "        }\n" +
//                "      }\n]\n}", JsonElement.class));
//        return jsonObject;
//    }
//
//    @Test
//    public void getHelpTextTest() {
//        assertThat(enableClusterMonitoring.getHelpText(), is(notNullValue()));
//    }
}
