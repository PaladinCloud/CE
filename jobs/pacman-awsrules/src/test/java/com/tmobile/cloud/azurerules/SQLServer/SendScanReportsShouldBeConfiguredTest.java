package com.tmobile.cloud.azurerules.SQLServer;

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
public class SendScanReportsShouldBeConfiguredTest {
    @InjectMocks
    SendScanReportsShouldBeConfigured sendScanReportsShouldBeConfigured;
    @Test
    public void executeSucessTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);

        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(), anyObject(), anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForSendScanReports());
        assertThat(sendScanReportsShouldBeConfigured.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    private JsonObject getHitJsonArrayForSendScanReports() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"azure_sqlserver\",\n" +
                "        \"_type\": \"sqlserver\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Sql/servers/paladin-server\",\n" +
                "        \"_score\": 0.18232156,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-10-20 06:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"region\": \"eastus\",\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Sql/servers/paladin-server\",\n" +
                "          \"kind\": \"v12.0\",\n" +
                "          \"name\": \"paladin-server\",\n" +
                "          \"regionName\": \"eastus\",\n" +
                "          \"state\": \"Ready\",\n" +
                "          \"systemAssignedManagedServiceIdentityPrincipalId\": \"cd096c8e-40c4-42a4-9a73-1233ec2e1ced\",\n" +
                "          \"systemAssignedManagedServiceIdentityTenantId\": \"db8e92e6-bdc8-4d89-97c9-3e52cbe9d583\",\n" +
                "          \"tags\": {\n" +
                "            \"for\": \"dev\",\n" +
                "            \"Environment\": \"qa\",\n" +
                "            \"Application\": \"Jupiter\",\n" +
                "            \"created_by\": \"skchalla\"\n" +
                "          },\n" +
                "          \"version\": \"12.0\",\n" +
                "          \"administratorLogin\": \"pacbot\",\n" +
                "          \"elasticPoolList\": [],\n" +
                "          \"failoverGroupList\": [],\n" +
                "          \"firewallRuleDetails\": [\n" +
                "            {\n" +
                "              \"name\": \"public access\",\n" +
                "              \"startIPAddress\": \"0.0.0.0\",\n" +
                "              \"endIPAddress\": \"0.0.0.0\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"storageContainerPath\": \"https://sqlserverstorageaccount1.blob.core.windows.net/vulnerability-assessment/\",\n" +
                "          \"recurringScansEnabled\": true,\n" +
                "          \"emailSubscriptionAdmins\": true,\n" +
                "          \"emails\": \"anjali-madhavi.nakirikanti@paladincloud.io\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Sql/servers/paladin-server\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Sql/servers/paladin-server\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"sqlserver\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-10-20 07:00:00+0000\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForSendScanReports());
        assertThat(sendScanReportsShouldBeConfigured.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));

    }

    private JsonObject getFailureJsonArrayForSendScanReports() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"azure_sqlserver\",\n" +
                "        \"_type\": \"sqlserver\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Sql/servers/paladin-server\",\n" +
                "        \"_score\": 0.18232156,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-10-20 07:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"region\": \"eastus\",\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Sql/servers/paladin-server\",\n" +
                "          \"kind\": \"v12.0\",\n" +
                "          \"name\": \"paladin-server\",\n" +
                "          \"regionName\": \"eastus\",\n" +
                "          \"state\": \"Ready\",\n" +
                "          \"systemAssignedManagedServiceIdentityPrincipalId\": \"cd096c8e-40c4-42a4-9a73-1233ec2e1ced\",\n" +
                "          \"systemAssignedManagedServiceIdentityTenantId\": \"db8e92e6-bdc8-4d89-97c9-3e52cbe9d583\",\n" +
                "          \"tags\": {\n" +
                "            \"for\": \"dev\",\n" +
                "            \"Environment\": \"qa\",\n" +
                "            \"Application\": \"Jupiter\",\n" +
                "            \"created_by\": \"skchalla\"\n" +
                "          },\n" +
                "          \"version\": \"12.0\",\n" +
                "          \"administratorLogin\": \"pacbot\",\n" +
                "          \"elasticPoolList\": [],\n" +
                "          \"failoverGroupList\": [],\n" +
                "          \"firewallRuleDetails\": [\n" +
                "            {\n" +
                "              \"name\": \"public access\",\n" +
                "              \"startIPAddress\": \"0.0.0.0\",\n" +
                "              \"endIPAddress\": \"0.0.0.0\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"storageContainerPath\": null,\n" +
                "          \"recurringScansEnabled\": false,\n" +
                "          \"emailSubscriptionAdmins\": true,\n" +
                "          \"emails\": null,\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Sql/servers/paladin-server\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Sql/servers/paladin-server\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"sqlserver\",\n" +
                "          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-10-20 07:38:00+0000\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]}", JsonElement.class));
        return jsonObject;
    }


    @Test
    public void getHelpTextTest() {
        assertThat(sendScanReportsShouldBeConfigured.getHelpText(), is(notNullValue()));
    }

}
