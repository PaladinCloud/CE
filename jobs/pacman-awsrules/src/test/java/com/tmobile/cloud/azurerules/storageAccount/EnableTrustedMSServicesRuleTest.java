package com.tmobile.cloud.azurerules.storageAccount;
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
import com.tmobile.cloud.azurerules.StorageAccount.EnableTrustedMSServices;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.StorageAccount.StorageAccountCMKEncryptionRule;
import com.tmobile.pacman.commons.rule.BaseRule;
@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class })
public class EnableTrustedMSServicesRuleTest {
    @InjectMocks
    EnableTrustedMSServices enableTrustedMSServices;

    public JsonObject getFailureJsonArrayForStorageAccountTrustMSServices(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\"discoverydate\":\"2022-08-0912:00:00+0000\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":null,\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"databricks-rg-dev-paladin-wdjpyrqd4kvis\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Storage/storageAccounts/dbstorageviepucpkuuclc\",\\\"canAccessFromAzureServices\\\":true,\\\"name\\\":\\\"dbstorageviepucpkuuclc\\\",\\\"regionName\\\":\\\"eastus\\\",\\\"customerManagedKey\\\":null,\\\"systemAssignedManagedServiceIdentityPrincipalId\\\":null,\\\"systemAssignedManagedServiceIdentityTenantId\\\":null,\\\"endPoints\\\":null,\\\"ipAddressesWithAccess\\\":[],\\\"ipAddressRangesWithAccess\\\":[],\\\"networkSubnetsWithAccess\\\":[],\\\"tags\\\":{\\\"Environment\\\":\\\"qa\\\",\\\"application\\\":\\\"Jupiter\\\",\\\"Application\\\":\\\"Jupiter\\\",\\\"databricks-environment\\\":\\\"true\\\"},\\\"kind\\\":\\\"BlobStorage\\\",\\\"endpointsMap\\\":{\\\"webEndPoint\\\":null,\\\"dfsEndPoint\\\":\\\"https://dbstorageviepucpkuuclc.dfs.core.windows.net/\\\",\\\"queueEndPoint\\\":null,\\\"tableEndPoint\\\":\\\"https://dbstorageviepucpkuuclc.table.core.windows.net/\\\",\\\"blobEndPoint\\\":\\\"https://dbstorageviepucpkuuclc.blob.core.windows.net/\\\",\\\"fileEndPoint\\\":null},\\\"accessAllowedFromAllNetworks\\\":true,\\\"azureFilesAadIntegrationEnabled\\\":false,\\\"hnsEnabled\\\":false,\\\"blobPublicAccessAllowed\\\":false,\\\"networkRuleBypass\\\":\\\"None\\\"} ]\n\n}\n}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonrrayForStorageAccountTrustMSServices() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\"discoverydate\":\"2022-08-0912:00:00+0000\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":null,\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"databricks-rg-dev-paladin-wdjpyrqd4kvis\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Storage/storageAccounts/dbstorageviepucpkuuclc\",\\\"canAccessFromAzureServices\\\":true,\\\"name\\\":\\\"dbstorageviepucpkuuclc\\\",\\\"regionName\\\":\\\"eastus\\\",\\\"customerManagedKey\\\":null,\\\"systemAssignedManagedServiceIdentityPrincipalId\\\":null,\\\"systemAssignedManagedServiceIdentityTenantId\\\":null,\\\"endPoints\\\":null,\\\"ipAddressesWithAccess\\\":[],\\\"ipAddressRangesWithAccess\\\":[],\\\"networkSubnetsWithAccess\\\":[],\\\"tags\\\":{\\\"Environment\\\":\\\"qa\\\",\\\"application\\\":\\\"Jupiter\\\",\\\"Application\\\":\\\"Jupiter\\\",\\\"databricks-environment\\\":\\\"true\\\"},\\\"kind\\\":\\\"BlobStorage\\\",\\\"endpointsMap\\\":{\\\"webEndPoint\\\":null,\\\"dfsEndPoint\\\":\\\"https://dbstorageviepucpkuuclc.dfs.core.windows.net/\\\",\\\"queueEndPoint\\\":null,\\\"tableEndPoint\\\":\\\"https://dbstorageviepucpkuuclc.table.core.windows.net/\\\",\\\"blobEndPoint\\\":\\\"https://dbstorageviepucpkuuclc.blob.core.windows.net/\\\",\\\"fileEndPoint\\\":null},\\\"accessAllowedFromAllNetworks\\\":true,\\\"azureFilesAadIntegrationEnabled\\\":false,\\\"hnsEnabled\\\":false,\\\"blobPublicAccessAllowed\\\":false,\\\"networkRuleBypass\\\":\\\"AzureServices\\\"} ]\n\n}\n}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonrrayForStorageAccountTrustMSServices());
        assertThat(enableTrustedMSServices.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForStorageAccountTrustMSServices());
        assertThat(enableTrustedMSServices.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(enableTrustedMSServices.getHelpText(), is(notNullValue()));
    }

}
