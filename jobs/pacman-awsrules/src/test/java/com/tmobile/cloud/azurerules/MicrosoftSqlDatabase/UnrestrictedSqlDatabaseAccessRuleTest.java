package com.tmobile.cloud.azurerules.MicrosoftSqlDatabase;

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
import com.tmobile.cloud.azurerules.MicrosoftSqlServer.UnrestrictedSqlServerAccessRule;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.BaseRule;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class})
public class UnrestrictedSqlDatabaseAccessRuleTest {
    @InjectMocks
    UnrestrictedSqlDatabaseAccessRule unrestrictedSqlDatabaseAccessRule;


    public JsonObject getFailureJsonArrayForUnrestrictedSqlDatabaseAccess(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\": \"2022-06-28 06:00:00+0000\",\n                \"_cloudType\": \"Azure\",\n                \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n                \"region\": \"centralus\",\n                \"subscriptionName\": \"dev-paladincloud\",\n                \"resourceGroupName\": \"dev-paladincloud\",\n                \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkSecurityGroups/testing-nsg\",\n                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n                \"name\": \"testing-nsg\",\n                \"tags\": {},\n                \"passwordBasedAuthenticationDisabled\": \"true\",\n                \"firewallRuleDetails\": [\n                    {\n                        \"startIPAddress\": \"0.0.0.0\"\n                    }\n                ],\n                \"notificationRecipientsEmails\": \"sqlserver\",\n                 \"inBoundSecurityRules\":[\n                     {\n                         \"sourceAddressPrefixes\":[ \"*\",\"any\",\"internet\"\n                         ],\n                         \"protocol\":\"TCP\",\n                         \"destinationPortRanges\":[\"*\",\"3306\"\n                         ]\n\n                     }\n                 ]\n            }\n        }\n    ]\n}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getHitJsonArrayForUnrestrictedSqlDatabaseAccess() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n        {\n            \"_source\": {\n                \"discoverydate\": \"2022-06-28 06:00:00+0000\",\n                \"_cloudType\": \"Azure\",\n                \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n                \"region\": \"centralus\",\n                \"subscriptionName\": \"dev-paladincloud\",\n                \"resourceGroupName\": \"dev-paladincloud\",\n                \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.Network/networkSecurityGroups/testing-nsg\",\n                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n                \"name\": \"testing-nsg\",\n                \"tags\": {},\n                \"passwordBasedAuthenticationDisabled\": \"true\",\n                \"firewallRuleDetails\": [\n                    {\n                        \"startIPAddress\": \"0.0.0.0\"\n                    }\n                ],\n                \"notificationRecipientsEmails\": \"sqlserver\",\n                 \"inBoundSecurityRules\":[\n                     {\n                         \"sourceAddressPrefixes\":[ \"*\",\"any\",\"internet\"\n                         ],\n                         \"protocol\":\"TCP\",\n                         \"destinationPortRanges\":[\"*\",\"3306\"\n                         ]\n\n                     }\n                 ]\n            }\n        }\n    ]\n}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForUnrestrictedSqlDatabaseAccess());
        assertThat(unrestrictedSqlDatabaseAccessRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForUnrestrictedSqlDatabaseAccess());
        assertThat(unrestrictedSqlDatabaseAccessRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(unrestrictedSqlDatabaseAccessRule.getHelpText(), is(notNullValue()));
    }

}