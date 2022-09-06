package com.tmobile.cloud.azurerules.MySQLFlexibleServer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.azurerules.FunctionApp.IncomingClientCertificateShouldBeEnabled;
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
public class MinTLSVersionRuleTest {
    @InjectMocks
    MinTLSVersionRule minTLSVersionRule;

    public JsonObject getFailureJsonArrayForMinTLSVersion(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n   {\n" +
                "        \"_index\": \"azure_mysqlflexible\",\n" +
                "        \"_type\": \"mysqlflexible\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.DBforMySQL/flexibleServers/paladin-mysql-tls1\",\n" +
                "        \"_score\": 1,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-09-06 10:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": \"dev-paladincloud\",\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.DBforMySQL/flexibleServers/paladin-mysql-tls1\",\n" +
                "          \"tlsVersion\": \"TLSV1\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.DBforMySQL/flexibleServers/paladin-mysql-tls1\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladincloud/providers/Microsoft.DBforMySQL/flexibleServers/paladin-mysql-tls1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"mysqlflexible\",\n" +
                "          \"firstdiscoveredon\": \"2022-09-05 20:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-09-06 10:24:00+0000\"\n" +
                "        }\n" +
                "      }    \n  ]\n}", JsonElement.class));
        return jsonObject;
    }

    public  JsonObject getHitJsonArrayForMinTLSVersion() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n    \"hits\": [\n     {\n" +
                "        \"_index\": \"azure_mysqlflexible\",\n" +
                "        \"_type\": \"mysqlflexible\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.DBforMySQL/flexibleServers/paladin-mysql1\",\n" +
                "        \"_score\": 1,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-09-06 10:00:00+0000\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": null,\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": \"dev-paladin-policydata-validation-rg\",\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.DBforMySQL/flexibleServers/paladin-mysql1\",\n" +
                "          \"tlsVersion\": \"TLSv1.2\",\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.DBforMySQL/flexibleServers/paladin-mysql1\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-paladin-policydata-validation-rg/providers/Microsoft.DBforMySQL/flexibleServers/paladin-mysql1\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"mysqlflexible\",\n" +
                "          \"firstdiscoveredon\": \"2022-09-05 13:00:00+0000\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-09-06 10:24:00+0000\"\n" +
                "        }\n" +
                "      }    ]\n}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForMinTLSVersion());
        assertThat(minTLSVersionRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForMinTLSVersion());
        assertThat(minTLSVersionRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }


    @Test
    public void getHelpTextTest() {
        assertThat(minTLSVersionRule.getHelpText(), is(notNullValue()));
    }

}
