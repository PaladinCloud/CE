package com.tmobile.cloud.azurerules.DiskRule;


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
public class CMKEncryptionForOsAndDataDiskRuleTest {

    @InjectMocks
    CMKEncryptionForOsAndDataDiskRule cmkEncryptionForOsAndDataDiskRule;

    public JsonObject getFailureForCMKEncryption() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\n" +
                        "    \"hits\": [{n\" +\n" +
                        "    \"_index\": \"azure_storageaccount\",n\" +\n" +
                        "    \"_type\": \"storageaccount\",n\" +\n" +
                        "    \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",n\" +\n" +
                        "     \"_score\": 1,n\" +\n" +
                        "      \"_source\": {n\" +\n" +
                        "       \"discoverydate\": \"2022-09-06 13:00:00+0530\",n\" +\n" +
                        "     \"_cloudType\": \"Azure\",n\" +\n" +
                        "        \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",n\" +\n" +
                        "       \"region\": null,n\" +\n" +
                        "       \"subscriptionName\": \"dev-paladincloud\",n\" +\n" +
                        "        \"resourceGroupName\": \"dev-rg-1\",n\" +\n" +
                        "         \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",n\" +\n" +
                        "        \"canAccessFromAzureServices\": true,n\" +\n" +
                        "       \"name\": \"sqlvaisndaov4vhjgg\",n\" +\n" +
                        "        \"regionName\": \"eastus\",n\" +\n" +
                        "         \"diskInner\": {n\" +\n" +
                        "         \"properties.encryption\": {n\" +\n" +
                        "          \"diskEncryptionSetId\": null,n\" +\n" +
                        "          \"type\": \"EncryptionAtRestWithPlatformKey\n\" +\n" +
                        "         \"}\" +\n" +
                        "         \"}\" +\n" +
                        "           \"}\" +\n" +
                        "           \"}]\" +\n" +
                        "           \"}",
                JsonElement.class));
        return jsonObject;
    }

    public JsonObject getHitJsonArrayForCMKEncryption() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\n" +
                        "\"hits\": [{\n" +
                        "\"_index\": \"azure_storageaccount\",\n" +
                        "\"_type\": \"storageaccount\",\n" +
                        "\"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n" +
                        "\"_score\": 1,\n" +
                        "\"_source\": {\n" +
                        "\"discoverydate\": \"2022-09-06 13:00:00+0530\",\n" +
                        "\"_cloudType\": \"Azure\",\n" +
                        "\"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "\"region\": null,\n" +
                        "\"subscriptionName\": \"dev-paladincloud\",\n" +
                        "\"resourceGroupName\": \"dev-rg-1\",\n" +
                        "\"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n" +
                        "\"canAccessFromAzureServices\": true,\n" +
                        "\"name\": \"sqlvaisndaov4vhjgg\",\n" +
                        "\"regionName\": \"eastus\",\n" +
                        "\"diskInner\": {\n" +
                        "\"properties.encryption\": {\n" +
                        "\"diskEncryptionSetId\": null,\n" +
                        "\"type\": \"EncryptionAtRestWithCustomerKey\"\n" +
                        "\"}\n" +
                        "\"}\n" +
                        "\"}\n" +
                        "\"}]\n" +
                        "}",
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
                .thenReturn(getHitJsonArrayForCMKEncryption());
        assertThat(cmkEncryptionForOsAndDataDiskRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                .thenReturn(getFailureForCMKEncryption());
        assertThat(cmkEncryptionForOsAndDataDiskRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(cmkEncryptionForOsAndDataDiskRule.getHelpText(), is(notNullValue()));
    }
}
