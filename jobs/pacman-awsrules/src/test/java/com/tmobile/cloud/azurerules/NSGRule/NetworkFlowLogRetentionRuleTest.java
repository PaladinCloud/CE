package com.tmobile.cloud.azurerules.NSGRule;


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

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class })
public class NetworkFlowLogRetentionRuleTest {
    @InjectMocks
    NetworkFlowLogRetentionRule networkFlowLogRetentionRule;

    public JsonObject getFailureJsonArrayForNetworkFlowLogRetention(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\":[{\"_source\":{\"discoverydate\":\"2022-09-2014:00:00+0530\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":\"eastus\",\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"databricks-rg-dev-paladin-wdjpyrqd4kvis\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/networkSecurityGroups/workers-sg\",\"key\":\"592fdcfa-bdff-4cf8-9090-a80ff273a6d7\",\"name\":\"workers-sg\",\"tags\":{\"Environment\":\"qa\",\"application\":\"Jupiter\",\"Application\":\"Jupiter\",\"databricks-environment\":\"true\"},\"networkInterfaceIds\":[],\"subnetList\":[{\"addressPrefix\":\"10.139.0.0/18\",\"name\":\"public-subnet\",\"vnet\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/virtualNetworks/workers-vnet\"},{\"addressPrefix\":\"10.139.64.0/18\",\"name\":\"private-subnet\",\"vnet\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/virtualNetworks/workers-vnet\"}],\"inBoundSecurityRules\":[{\"description\":\"RequiredforDatabrickscontrolplanemanagementofworkernodes.\",\"access\":\"Allow\",\"priority\":100,\"name\":\"databricks-control-plane-ssh\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"22\"],\"sourceAddressPrefixes\":[\"20.42.4.208/32\",\"20.42.4.210/32\",\"23.101.152.95/32\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":false},{\"description\":\"RequiredforDatabrickscontrolplanecommunicationwithworkernodes.\",\"access\":\"Allow\",\"priority\":110,\"name\":\"databricks-control-plane-worker-proxy\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"5557\"],\"sourceAddressPrefixes\":[\"20.42.4.208/32\",\"20.42.4.210/32\",\"23.101.152.95/32\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":false},{\"description\":\"Requiredforworkernodescommunicationwithinacluster.\",\"access\":\"Allow\",\"priority\":200,\"name\":\"databricks-worker-to-worker\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"VirtualNetwork\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":false},{\"description\":\"Allowinboundtrafficfromazureloadbalancer\",\"access\":\"Allow\",\"priority\":65001,\"name\":\"AllowAzureLoadBalancerInBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"AzureLoadBalancer\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true},{\"description\":\"AllowinboundtrafficfromallVMsinVNET\",\"access\":\"Allow\",\"priority\":65000,\"name\":\"AllowVnetInBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"VirtualNetwork\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"VirtualNetwork\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true},{\"description\":\"Denyallinboundtraffic\",\"access\":\"Deny\",\"priority\":65500,\"name\":\"DenyAllInBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"*\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true}],\"outBoundSecurityRules\":[{\"description\":\"AllowoutboundtrafficfromallVMstoInternet\",\"access\":\"Allow\",\"priority\":65001,\"name\":\"AllowInternetOutBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"Internet\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"*\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true},{\"description\":\"AllowoutboundtrafficfromallVMstoallVMsinVNET\",\"access\":\"Allow\",\"priority\":65000,\"name\":\"AllowVnetOutBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"VirtualNetwork\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"VirtualNetwork\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true},{\"description\":\"Denyalloutboundtraffic\",\"access\":\"Deny\",\"priority\":65500,\"name\":\"DenyAllOutBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"*\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true}],\"networkWatcher\":[{\"name\":\"NetworkWatcher_eastus\",\"id\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/NetworkWatcherRG/providers/Microsoft.Network/networkWatchers/NetworkWatcher_eastus\",\"enabled\":false,\"retentionInDays\":0,\"retentionEnabled\":false}],\"_resourceid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/networkSecurityGroups/workers-sg\",\"_docid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/networkSecurityGroups/workers-sg\",\"_entity\":\"true\",\"_entitytype\":\"nsg\",\"firstdiscoveredon\":\"2022-07-2015:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-09-2009:06:00+0000\"}}]}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonArrayForNetworkFlowLogRetention() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\":[{\"_source\":{\"discoverydate\":\"2022-09-2014:00:00+0530\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":\"eastus\",\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"databricks-rg-dev-paladin-wdjpyrqd4kvis\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/networkSecurityGroups/workers-sg\",\"key\":\"592fdcfa-bdff-4cf8-9090-a80ff273a6d7\",\"name\":\"workers-sg\",\"tags\":{\"Environment\":\"qa\",\"application\":\"Jupiter\",\"Application\":\"Jupiter\",\"databricks-environment\":\"true\"},\"networkInterfaceIds\":[],\"subnetList\":[{\"addressPrefix\":\"10.139.0.0/18\",\"name\":\"public-subnet\",\"vnet\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/virtualNetworks/workers-vnet\"},{\"addressPrefix\":\"10.139.64.0/18\",\"name\":\"private-subnet\",\"vnet\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/virtualNetworks/workers-vnet\"}],\"inBoundSecurityRules\":[{\"description\":\"RequiredforDatabrickscontrolplanemanagementofworkernodes.\",\"access\":\"Allow\",\"priority\":100,\"name\":\"databricks-control-plane-ssh\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"22\"],\"sourceAddressPrefixes\":[\"20.42.4.208/32\",\"20.42.4.210/32\",\"23.101.152.95/32\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":false},{\"description\":\"RequiredforDatabrickscontrolplanecommunicationwithworkernodes.\",\"access\":\"Allow\",\"priority\":110,\"name\":\"databricks-control-plane-worker-proxy\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"5557\"],\"sourceAddressPrefixes\":[\"20.42.4.208/32\",\"20.42.4.210/32\",\"23.101.152.95/32\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":false},{\"description\":\"Requiredforworkernodescommunicationwithinacluster.\",\"access\":\"Allow\",\"priority\":200,\"name\":\"databricks-worker-to-worker\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"VirtualNetwork\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":false},{\"description\":\"Allowinboundtrafficfromazureloadbalancer\",\"access\":\"Allow\",\"priority\":65001,\"name\":\"AllowAzureLoadBalancerInBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"AzureLoadBalancer\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true},{\"description\":\"AllowinboundtrafficfromallVMsinVNET\",\"access\":\"Allow\",\"priority\":65000,\"name\":\"AllowVnetInBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"VirtualNetwork\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"VirtualNetwork\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true},{\"description\":\"Denyallinboundtraffic\",\"access\":\"Deny\",\"priority\":65500,\"name\":\"DenyAllInBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"*\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true}],\"outBoundSecurityRules\":[{\"description\":\"AllowoutboundtrafficfromallVMstoInternet\",\"access\":\"Allow\",\"priority\":65001,\"name\":\"AllowInternetOutBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"Internet\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"*\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true},{\"description\":\"AllowoutboundtrafficfromallVMstoallVMsinVNET\",\"access\":\"Allow\",\"priority\":65000,\"name\":\"AllowVnetOutBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"VirtualNetwork\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"VirtualNetwork\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true},{\"description\":\"Denyalloutboundtraffic\",\"access\":\"Deny\",\"priority\":65500,\"name\":\"DenyAllOutBound\",\"protocol\":\"*\",\"destinationAddressPrefixes\":[\"*\"],\"destinationApplicationSecurityGroupIds\":[],\"destinationPortRanges\":[\"*\"],\"sourceAddressPrefixes\":[\"*\"],\"sourceApplicationSecurityGroupIds\":[],\"sourcePortRanges\":[\"*\"],\"default\":true}],\"networkWatcher\":[{\"name\":\"NetworkWatcher_eastus\",\"id\":\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/NetworkWatcherRG/providers/Microsoft.Network/networkWatchers/NetworkWatcher_eastus\",\"enabled\":false,\"retentionInDays\":130,\"retentionEnabled\":true}],\"_resourceid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/networkSecurityGroups/workers-sg\",\"_docid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Network/networkSecurityGroups/workers-sg\",\"_entity\":\"true\",\"_entitytype\":\"nsg\",\"firstdiscoveredon\":\"2022-07-2015:00:00+0000\",\"latest\":true,\"_loaddate\":\"2022-09-2009:06:00+0000\"}}]}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForNetworkFlowLogRetention());
        assertThat(networkFlowLogRetentionRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForNetworkFlowLogRetention());
        assertThat(networkFlowLogRetentionRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(networkFlowLogRetentionRule.getHelpText(), is(notNullValue()));
    }

}
