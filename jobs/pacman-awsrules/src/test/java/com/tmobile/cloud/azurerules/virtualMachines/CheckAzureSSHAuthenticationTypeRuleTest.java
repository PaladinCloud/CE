package com.tmobile.cloud.azurerules.virtualMachines;

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
import com.tmobile.pacman.commons.PacmanSdkConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.policies.CheckAzureSSHAuthenticationTypeRule;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.BaseRule;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class})
public class CheckAzureSSHAuthenticationTypeRuleTest {
    @InjectMocks
    CheckAzureSSHAuthenticationTypeRule checkAzureSSHAuthenticationTypeRule;


    public JsonObject getFailureJsonArrayForAzureSSHAuthentication(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\":\"2022-07-03 11:00:00+0000\",\n                \"_cloudType\": \"Azure\",\n                \"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n                \"region\":\"centralus\",\n                \"subscriptionName\": \"dev-paladincloud\",\n                \"resourceGroupName\": \"dev-paladincloud\",\n                \"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/DEV-PALADINCLOUD/providers/Microsoft.Compute/virtualMachines/testing\",\n                \"networkInterfaceIds\":[\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkInterfaces/testing243\"],\n                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n                \"disks\":[\n                    {\"storageAccountType\":\"Unknown\",\n                    \"name\":\"testing_disk1_82e2c9d0e28746c18f86e7dc52694290\",\n                    \"sizeInGB\":\"null\",\n                    \"type\":\"OSDisk\",\n                    \"cachingType\":\"ReadWrite\",\n                    \"isEncryptionEnabled\":\"false\"\n                    }\n                    ],\n                \"passwordBasedAuthenticationDisabled\":\"false\"\n            }\n        }\n    ]\n}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonArrayForAzureSSHAuthentication() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\":\"2022-07-03 11:00:00+0000\",\n                \"_cloudType\": \"Azure\",\n                \"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n                \"region\":\"centralus\",\n                \"subscriptionName\": \"dev-paladincloud\",\n                \"resourceGroupName\": \"dev-paladincloud\",\n                \"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/DEV-PALADINCLOUD/providers/Microsoft.Compute/virtualMachines/testing\",\n                \"networkInterfaceIds\":[\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkInterfaces/testing243\"],\n                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n                \"disks\":[\n                    {\"storageAccountType\":\"Unknown\",\n                    \"name\":\"testing_disk1_82e2c9d0e28746c18f86e7dc52694290\",\n                    \"sizeInGB\":\"null\",\n                    \"type\":\"OSDisk\",\n                    \"cachingType\":\"ReadWrite\",\n                    \"isEncryptionEnabled\":\"false\"\n                    }\n                    ],\n                \"passwordBasedAuthenticationDisabled\":\"true\"\n            }\n        }\n    ]\n}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForAzureSSHAuthentication());
        assertThat(checkAzureSSHAuthenticationTypeRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForAzureSSHAuthentication());
        assertThat(checkAzureSSHAuthenticationTypeRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }


    @Test
    public void getHelpTextTest() {
        assertThat(checkAzureSSHAuthenticationTypeRule.getHelpText(), is(notNullValue()));
    }

}