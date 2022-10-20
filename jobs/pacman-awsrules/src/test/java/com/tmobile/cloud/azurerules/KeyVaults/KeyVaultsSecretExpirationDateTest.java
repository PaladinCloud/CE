package com.tmobile.cloud.azurerules.KeyVaults;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.azurerules.KeyValts.KeyVaultsSecretExpirationDate;
import com.tmobile.pacman.commons.rule.BaseRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import org.junit.Test;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class})
public class KeyVaultsSecretExpirationDateTest {
    @InjectMocks
    KeyVaultsSecretExpirationDate keyVaultsSecretExpirationDate;
    @Test
    public void executeSucessTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForSecretExpirationDate());
        assertThat(keyVaultsSecretExpirationDate.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    private JsonObject getHitJsonArrayForSecretExpirationDate() {
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n {\n" +
                "        \"_index\": \"azure_vaults\",\n" +
                "        \"_type\": \"vaults\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.KeyVault/vaults/policy-keyvault1\",\n" +
                "        \"_score\": 0.105360515,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-10-12 14:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.KeyVault/vaults/policy-keyvault1\",\n" +
                "          \"name\": \"policy-keyvault1\",\n" +
                "          \"type\": \"Microsoft.KeyVault/vaults\",\n" +
                "          \"location\": \"eastus\",\n" +
                "          \"tags\": {},\n" +
                "          \"sku\": {\n" +
                "            \"family\": \"A\",\n" +
                "            \"name\": \"Standard\"\n" +
                "          },\n" +
                "          \"enabledForDeployment\": false,\n" +
                "          \"enabledForDiskEncryption\": false,\n" +
                "          \"enabledForTemplateDeployment\": false,\n" +
                "          \"tenantId\": \"db8e92e6-bdc8-4d89-97c9-3e52cbe9d583\",\n" +
                "          \"provisioningState\": \"Succeeded\",\n" +
                "          \"vaultUri\": \"https://policy-keyvault1.vault.azure.net/\",\n" +
                "          \"permissionForKeys\": [\n" +
                "            \"Get\",\n" +
                "            \"List\",\n" +
                "            \"Update\",\n" +
                "            \"Create\",\n" +
                "            \"Import\",\n" +
                "            \"Delete\",\n" +
                "            \"Recover\",\n" +
                "            \"Backup\",\n" +
                "            \"Restore\",\n" +
                "            \"GetRotationPolicy\",\n" +
                "            \"SetRotationPolicy\",\n" +
                "            \"Rotate\"\n" +
                "          ],\n" +
                "          \"permissionForSecrets\": [\n" +
                "            \"Get\",\n" +
                "            \"List\",\n" +
                "            \"Set\",\n" +
                "            \"Delete\",\n" +
                "            \"Recover\",\n" +
                "            \"Backup\",\n" +
                "            \"Restore\"\n" +
                "          ],\n" +
                "          \"permissionForCertificates\": [\n" +
                "            \"Get\",\n" +
                "            \"List\",\n" +
                "            \"Update\",\n" +
                "            \"Create\",\n" +
                "            \"Import\",\n" +
                "            \"Delete\",\n" +
                "            \"Recover\",\n" +
                "            \"Backup\",\n" +
                "            \"Restore\",\n" +
                "            \"ManageContacts\",\n" +
                "            \"ManageIssuers\",\n" +
                "            \"GetIssuers\",\n" +
                "            \"ListIssuers\",\n" +
                "            \"SetIssuers\",\n" +
                "            \"DeleteIssuers\"\n" +
                "          ],\n" +
                "          \"secretExpirationDate\": [\n" +
                "            \"2024-10-12T14:06:44.000Z\"\n" +
                "          ],\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.KeyVault/vaults/policy-keyvault1\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.KeyVault/vaults/policy-keyvault1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vaults\",\n" +
                "          \"firstdiscoveredon\": \"2022-09-26 05:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-10-12 14:57:00+0000\"\n" +
                "        }\n" +
                "      } \n]\n}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForSecretExpirationDate());
        assertThat(keyVaultsSecretExpirationDate.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    private JsonObject getFailureJsonArrayForSecretExpirationDate() {
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n{\n" +
                "        \"_index\": \"azure_vaults\",\n" +
                "        \"_type\": \"vaults\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.KeyVault/vaults/new-test-6\",\n" +
                "        \"_score\": 0.13353139,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-10-12 14:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.KeyVault/vaults/new-test-6\",\n" +
                "          \"name\": \"new-test-6\",\n" +
                "          \"type\": \"Microsoft.KeyVault/vaults\",\n" +
                "          \"location\": \"eastus\",\n" +
                "          \"tags\": {},\n" +
                "          \"sku\": {\n" +
                "            \"family\": \"A\",\n" +
                "            \"name\": \"Standard\"\n" +
                "          },\n" +
                "          \"enabledForDeployment\": true,\n" +
                "          \"enabledForDiskEncryption\": true,\n" +
                "          \"enabledForTemplateDeployment\": true,\n" +
                "          \"tenantId\": \"db8e92e6-bdc8-4d89-97c9-3e52cbe9d583\",\n" +
                "          \"provisioningState\": \"Succeeded\",\n" +
                "          \"vaultUri\": \"https://new-test-6.vault.azure.net/\",\n" +
                "          \"permissionForKeys\": null,\n" +
                "          \"permissionForSecrets\": null,\n" +
                "          \"permissionForCertificates\": null,\n" +
                "          \"secretExpirationDate\": null,\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.KeyVault/vaults/new-test-6\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.KeyVault/vaults/new-test-6\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"vaults\",\n" +
                "          \"firstdiscoveredon\": \"2022-08-30 15:00:00+0530\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-10-12 14:57:00+0000\"\n" +
                "        }\n" +
                "      }  \n]\n}", JsonElement.class));
        return jsonObject;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(keyVaultsSecretExpirationDate.getHelpText(), is(notNullValue()));
    }
}
