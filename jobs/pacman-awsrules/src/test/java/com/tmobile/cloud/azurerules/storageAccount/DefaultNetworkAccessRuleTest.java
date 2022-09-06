package com.tmobile.cloud.azurerules.storageAccount;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.azurerules.StorageAccount.DefaultNetworkAccess;
import com.tmobile.cloud.azurerules.StorageAccount.EnableTrustedMSServices;
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
public class DefaultNetworkAccessRuleTest {
    @InjectMocks
    DefaultNetworkAccess defaultNetworkAccess;

    public JsonObject getFailureJsonArrayForHttpsonly() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\n    \"hits\": [\n      {\n        \"_index\": \"azure_storageaccount\",\n        \"_type\": \"storageaccount\",\n        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n        \"_score\": 1,\n        \"_source\": {\n          \"discoverydate\": \"2022-09-06 13:00:00+0530\",\n          \"_cloudType\": \"Azure\",\n          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n          \"region\": null,\n          \"subscriptionName\": \"dev-paladincloud\",\n          \"resourceGroupName\": \"dev-rg-1\",\n          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n          \"canAccessFromAzureServices\": true,\n          \"name\": \"sqlvaisndaov4vhjgg\",\n          \"regionName\": \"eastus\",\n          \"customerManagedKey\": null,\n          \"systemAssignedManagedServiceIdentityPrincipalId\": null,\n          \"systemAssignedManagedServiceIdentityTenantId\": null,\n          \"endPoints\": null,\n          \"ipAddressesWithAccess\": [],\n          \"ipAddressRangesWithAccess\": [],\n          \"networkSubnetsWithAccess\": [],\n          \"tags\": {\n            \"Application\": \"Jupiter\",\n            \"Environment\": \"qa\"\n          },\n          \"kind\": \"StorageV2\",\n          \"endpointsMap\": {\n            \"webEndPoint\": \"https://sqlvaisndaov4vhjgg.z13.web.core.windows.net/\",\n            \"dfsEndPoint\": \"https://sqlvaisndaov4vhjgg.dfs.core.windows.net/\",\n            \"queueEndPoint\": \"https://sqlvaisndaov4vhjgg.queue.core.windows.net/\",\n            \"tableEndPoint\": \"https://sqlvaisndaov4vhjgg.table.core.windows.net/\",\n            \"blobEndPoint\": \"https://sqlvaisndaov4vhjgg.blob.core.windows.net/\",\n            \"fileEndPoint\": \"https://sqlvaisndaov4vhjgg.file.core.windows.net/\"\n          },\n          \"networkRuleBypass\": \"AzureServices\",\n          \"defaultAction\": \"ALLOW\",\n          \"accessAllowedFromAllNetworks\": false,\n          \"azureFilesAadIntegrationEnabled\": false,\n          \"hnsEnabled\": false,\n          \"blobPublicAccessAllowed\": true,\n          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n          \"_entity\": \"true\",\n          \"_entitytype\": \"storageaccount\",\n          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n          \"latest\": true,\n          \"_loaddate\": \"2022-09-06 08:26:00+0000\"\n        }\n      }\n         ]\n\n}",
                JsonElement.class));
        return jsonObject;
    }

    public JsonObject getHitJsonArrayForHttpsonly() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\n    \"hits\": [\n      {\n        \"_index\": \"azure_storageaccount\",\n        \"_type\": \"storageaccount\",\n        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n        \"_score\": 1,\n        \"_source\": {\n          \"discoverydate\": \"2022-09-06 13:00:00+0530\",\n          \"_cloudType\": \"Azure\",\n          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n          \"region\": null,\n          \"subscriptionName\": \"dev-paladincloud\",\n          \"resourceGroupName\": \"dev-rg-1\",\n          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n          \"canAccessFromAzureServices\": true,\n          \"name\": \"sqlvaisndaov4vhjgg\",\n          \"regionName\": \"eastus\",\n          \"customerManagedKey\": null,\n          \"systemAssignedManagedServiceIdentityPrincipalId\": null,\n          \"systemAssignedManagedServiceIdentityTenantId\": null,\n          \"endPoints\": null,\n          \"ipAddressesWithAccess\": [],\n          \"ipAddressRangesWithAccess\": [],\n          \"networkSubnetsWithAccess\": [],\n          \"tags\": {\n            \"Application\": \"Jupiter\",\n            \"Environment\": \"qa\"\n          },\n          \"kind\": \"StorageV2\",\n          \"endpointsMap\": {\n            \"webEndPoint\": \"https://sqlvaisndaov4vhjgg.z13.web.core.windows.net/\",\n            \"dfsEndPoint\": \"https://sqlvaisndaov4vhjgg.dfs.core.windows.net/\",\n            \"queueEndPoint\": \"https://sqlvaisndaov4vhjgg.queue.core.windows.net/\",\n            \"tableEndPoint\": \"https://sqlvaisndaov4vhjgg.table.core.windows.net/\",\n            \"blobEndPoint\": \"https://sqlvaisndaov4vhjgg.blob.core.windows.net/\",\n            \"fileEndPoint\": \"https://sqlvaisndaov4vhjgg.file.core.windows.net/\"\n          },\n          \"networkRuleBypass\": \"AzureServices\",\n          \"defaultAction\": \"DENY\",\n          \"accessAllowedFromAllNetworks\": false,\n          \"azureFilesAadIntegrationEnabled\": false,\n          \"hnsEnabled\": false,\n          \"blobPublicAccessAllowed\": true,\n          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n          \"_entity\": \"true\",\n          \"_entitytype\": \"storageaccount\",\n          \"firstdiscoveredon\": \"2022-07-20 15:00:00+0000\",\n          \"latest\": true,\n          \"_loaddate\": \"2022-09-06 08:26:00+0000\"\n        }\n      }\n         ]\n\n}",
                JsonElement.class));
        return jsonObject;
    }

    @Test
    public void executeSucessTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(), anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject()))
                .thenReturn(getHitJsonArrayForHttpsonly());
        assertThat(defaultNetworkAccess.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeFailureTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(), anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject()))
                .thenReturn(getFailureJsonArrayForHttpsonly());
        assertThat(defaultNetworkAccess.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(defaultNetworkAccess.getHelpText(), is(notNullValue()));
    }
}
