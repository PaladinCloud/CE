package com.tmobile.cloud.azurerules.storageAccount;

import com.amazonaws.services.identitymanagement.model.InvalidInputException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.azurerules.StorageAccount.StorageAccountCMKEncryptionRule;
import com.tmobile.cloud.azurerules.StorageAccount.StorageAccountPublicAcessRule;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.BaseRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
public class StorageAccountPublicAcessRuleTest {

    @InjectMocks
    StorageAccountPublicAcessRule storageAccountPublicAcessRule;


    public JsonObject getFailureJsonArrayForStorageAccountPublicAccess(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\":\"2022-07-03 11:00:00+0000\",\n                \"_cloudType\": \"Azure\",\n                \"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n                \"region\":\"null\",\n                \"subscriptionName\": \"dev-paladincloud\",\n                \"resourceGroupName\": \"dev-paladincloud\",\n                \"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n                \"canAccessFromAzureServices\":\"true\",\n                \"name\":\"sqlvaisndaov4vhjgg\",\n                \"regionName\":\"eastus\",\n                \"customerManagedKey\":\"dds123n3\",\n                \"blobPublicAccessAllowed\":\"false\",\n                \"tags\":{},\n                \"kind\":\"StorageV2\"\n            }\n        }\n    ]\n\n}\n}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonArrayForStorageAccountPublicAccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\":\"2022-07-03 11:00:00+0000\",\n                \"_cloudType\": \"Azure\",\n                \"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n                \"region\":\"null\",\n                \"subscriptionName\": \"dev-paladincloud\",\n                \"resourceGroupName\": \"dev-paladincloud\",\n                \"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n                \"canAccessFromAzureServices\":\"true\",\n                \"name\":\"sqlvaisndaov4vhjgg\",\n                \"regionName\":\"eastus\",\n                \"customerManagedKey\":\"dds123n3\",\n                \"blobPublicAccessAllowed\":\"true\",\n                \"tags\":{},\n                \"kind\":\"StorageV2\"\n            }\n        }\n    ]\n\n}\n}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForStorageAccountPublicAccess());
        assertThat(storageAccountPublicAcessRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForStorageAccountPublicAccess());
        assertThat(storageAccountPublicAcessRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(storageAccountPublicAcessRule.getHelpText(), is(notNullValue()));
    }
}
