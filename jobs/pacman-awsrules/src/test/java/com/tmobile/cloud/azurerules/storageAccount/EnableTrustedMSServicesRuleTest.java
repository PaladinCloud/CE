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
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.StorageAccount.StorageAccountCMKEncryptionRule;
@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BasePolicy.class, RulesElasticSearchRepositoryUtil.class, Annotation.class })
public class EnableTrustedMSServicesRuleTest {
    @InjectMocks
    EnableTrustedMSServices enableTrustedMSServices;

    public JsonObject getFailureJsonArrayForStorageAccountTrustMSServices(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n" +
                "    \"hits\": [\n" +
                "        {\n" +
                "            \"_source\": {\n" +
                "                \"discoverydate\": \"2022-08-0912:00:00+0000\",\n" +
                "                \"_cloudType\": \"Azure\",\n" +
                "                \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "                \"region\": null,\n" +
                "                \"subscriptionName\": \"dev-paladincloud\",\n" +
                "                \"resourceGroupName\": \"databricks-rg-dev-paladin-wdjpyrqd4kvis\",\n" +
                "                \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Storage/storageAccounts/dbstorageviepucpkuuclc\",\n" +
                "                \"canAccessFromAzureServices\": true,\n" +
                "                \"name\": \"dbstorageviepucpkuuclc\",\n" +
                "                \"regionName\": \"eastus\",\n" +
                "                \"customerManagedKey\": null,\n" +
                "                \"systemAssignedManagedServiceIdentityPrincipalId\": null,\n" +
                "                \"systemAssignedManagedServiceIdentityTenantId\": null,\n" +
                "                \"endPoints\": null,\n" +
                "                \"ipAddressesWithAccess\": [],\n" +
                "                \"ipAddressRangesWithAccess\": [],\n" +
                "                \"networkSubnetsWithAccess\": [],\n" +
                "                \"tags\": {\n" +
                "                    \"Environment\": \"qa\",\n" +
                "                    \"application\": \"Jupiter\",\n" +
                "                    \"databricks-environment\": \"true\"\n" +
                "                },\n" +
                "                \"kind\": \"BlobStorage\",\n" +
                "                \"endpointsMap\": {\n" +
                "                    \"webEndPoint\": null,\n" +
                "                    \"dfsEndPoint\": \"https://dbstorageviepucpkuuclc.dfs.core.windows.net/\",\n" +
                "                    \"queueEndPoint\": null,\n" +
                "                    \"tableEndPoint\": \"https://dbstorageviepucpkuuclc.table.core.windows.net/\",\n" +
                "                    \"blobEndPoint\": \"https://dbstorageviepucpkuuclc.blob.core.windows.net/\",\n" +
                "                    \"fileEndPoint\": null\n" +
                "                },\n" +
                "                \"accessAllowedFromAllNetworks\": true,\n" +
                "                \"azureFilesAadIntegrationEnabled\": false,\n" +
                "                \"hnsEnabled\": false,\n" +
                "                \"blobPublicAccessAllowed\": false,\n" +
                "                \"networkRuleBypass\": \"None\"\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}\n", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonrrayForStorageAccountTrustMSServices() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n" +
                "    \"hits\": [\n" +
                "        {\n" +
                "            \"_source\": {\n" +
                "                \"discoverydate\": \"2022-08-0912:00:00+0000\",\n" +
                "                \"_cloudType\": \"Azure\",\n" +
                "                \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "                \"region\": null,\n" +
                "                \"subscriptionName\": \"dev-paladincloud\",\n" +
                "                \"resourceGroupName\": \"databricks-rg-dev-paladin-wdjpyrqd4kvis\",\n" +
                "                \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/databricks-rg-dev-paladin-wdjpyrqd4kvis/providers/Microsoft.Storage/storageAccounts/dbstorageviepucpkuuclc\",\n" +
                "                \"canAccessFromAzureServices\": true,\n" +
                "                \"name\": \"dbstorageviepucpkuuclc\",\n" +
                "                \"regionName\": \"eastus\",\n" +
                "                \"customerManagedKey\": null,\n" +
                "                \"systemAssignedManagedServiceIdentityPrincipalId\": null,\n" +
                "                \"systemAssignedManagedServiceIdentityTenantId\": null,\n" +
                "                \"endPoints\": null,\n" +
                "                \"ipAddressesWithAccess\": [],\n" +
                "                \"ipAddressRangesWithAccess\": [],\n" +
                "                \"networkSubnetsWithAccess\": [],\n" +
                "                \"tags\": {\n" +
                "                    \"Environment\": \"qa\",\n" +
                "                    \"application\": \"Jupiter\",\n" +
                "                    \"Application\": \"Jupiter\",\n" +
                "                    \"databricks-environment\": \"true\"\n" +
                "                },\n" +
                "                \"kind\": \"BlobStorage\",\n" +
                "                \"endpointsMap\": {\n" +
                "                    \"webEndPoint\": null,\n" +
                "                    \"dfsEndPoint\": \"https://dbstorageviepucpkuuclc.dfs.core.windows.net/\",\n" +
                "                    \"queueEndPoint\": null,\n" +
                "                    \"tableEndPoint\": \"https://dbstorageviepucpkuuclc.table.core.windows.net/\",\n" +
                "                    \"blobEndPoint\": \"https://dbstorageviepucpkuuclc.blob.core.windows.net/\",\n" +
                "                    \"fileEndPoint\": null\n" +
                "                },\n" +
                "                \"accessAllowedFromAllNetworks\": true,\n" +
                "                \"azureFilesAadIntegrationEnabled\": false,\n" +
                "                \"hnsEnabled\": false,\n" +
                "                \"blobPublicAccessAllowed\": false,\n" +
                "                \"networkRuleBypass\": \"AzureServices\"\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}\n", JsonElement.class));
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
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(enableTrustedMSServices.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private Annotation getMockAnnotation() {
        Annotation annotation=new Annotation();
        annotation.put(PacmanSdkConstants.POLICY_NAME,"Mock policy name");
        annotation.put(PacmanSdkConstants.POLICY_ID, "Mock policy id");
        annotation.put(PacmanSdkConstants.POLICY_VERSION, "Mock policy version");
        annotation.put(PacmanSdkConstants.RESOURCE_ID, "Mock resource id");
        annotation.put(PacmanSdkConstants.TYPE, "Mock type");
        return annotation;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(enableTrustedMSServices.getHelpText(), is(notNullValue()));
    }

}
