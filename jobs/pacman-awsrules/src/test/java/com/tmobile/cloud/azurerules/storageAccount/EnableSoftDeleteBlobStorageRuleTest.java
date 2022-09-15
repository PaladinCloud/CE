package com.tmobile.cloud.azurerules.storageAccount;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.azurerules.StorageAccount.EnableSoftDeleteBlobStorageRule;
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
public class EnableSoftDeleteBlobStorageRuleTest {

    @InjectMocks
    EnableSoftDeleteBlobStorageRule enableSoftDeleteBlobStorageRule;

    public JsonObject getFailureJsonArrayForBlobServiceSoftDelete(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\":[{\"_index\":\"azure_blobservice\",\"_type\":\"blobservice\",\"_id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Storage/storageAccounts/devpaladinpolicydat9aa3/blobServices/default\",\"_score\":1,\"_source\":{\"discoverydate\":\"2022-09-1513:00:00+0530\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":null,\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":null,\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Storage/storageAccounts/devpaladinpolicydat9aa3/blobServices/default\",\"name\":\"default\",\"type\":\"Microsoft.Storage/storageAccounts/blobServices\",\"propertiesMap\":{\"cors\":{\"corsRules\":[]},\"deleteRetentionPolicy\":{\"allowPermanentDelete\":false,\"enabled\":false}},\"_resourceid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Storage/storageAccounts/devpaladinpolicydat9aa3/blobServices/default\",\"_docid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.Storage/storageAccounts/devpaladinpolicydat9aa3/blobServices/default\",\"_entity\":\"true\",\"_entitytype\":\"blobservice\",\"firstdiscoveredon\":\"2022-09-1513:00:00+0530\",\"latest\":true,\"_loaddate\":\"2022-09-1508:25:00+0000\"}}]}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonrrayForBlobServiceSoftDelete() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\":[{\"_index\":\"azure_blobservice\",\"_type\":\"blobservice\",\"_id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/rg-dev-paladincloud/providers/Microsoft.Storage/storageAccounts/tbrimmutable/blobServices/default\",\"_score\":1,\"_source\":{\"discoverydate\":\"2022-09-1513:00:00+0530\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":null,\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":null,\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/rg-dev-paladincloud/providers/Microsoft.Storage/storageAccounts/tbrimmutable/blobServices/default\",\"name\":\"default\",\"type\":\"Microsoft.Storage/storageAccounts/blobServices\",\"propertiesMap\":{\"containerDeleteRetentionPolicy\":{\"enabled\":true,\"days\":7},\"cors\":{\"corsRules\":[]},\"changeFeed\":{\"enabled\":false},\"restorePolicy\":{\"enabled\":false},\"isVersioningEnabled\":false,\"deleteRetentionPolicy\":{\"allowPermanentDelete\":false,\"enabled\":true,\"days\":7}},\"_resourceid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/rg-dev-paladincloud/providers/Microsoft.Storage/storageAccounts/tbrimmutable/blobServices/default\",\"_docid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/rg-dev-paladincloud/providers/Microsoft.Storage/storageAccounts/tbrimmutable/blobServices/default\",\"_entity\":\"true\",\"_entitytype\":\"blobservice\",\"firstdiscoveredon\":\"2022-09-1513:00:00+0530\",\"latest\":true,\"_loaddate\":\"2022-09-1508:25:00+0000\"}},]}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonrrayForBlobServiceSoftDelete());
        assertThat(enableSoftDeleteBlobStorageRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForBlobServiceSoftDelete());
        assertThat(enableSoftDeleteBlobStorageRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(enableSoftDeleteBlobStorageRule.getHelpText(), is(notNullValue()));
    }
}
