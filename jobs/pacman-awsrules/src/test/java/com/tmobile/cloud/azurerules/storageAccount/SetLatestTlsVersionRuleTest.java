package com.tmobile.cloud.azurerules.storageAccount;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.azurerules.StorageAccount.SetLatestTlsVersionRule;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*","jdk.internal.reflect.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BasePolicy.class, RulesElasticSearchRepositoryUtil.class, Annotation.class })
public class SetLatestTlsVersionRuleTest {

    @InjectMocks
    SetLatestTlsVersionRule setLatestTlsVersionRule;


    public JsonObject getFailureJsonArrayForMinTlsVersion() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\n" +
                        "    \"hits\": [\n" +
                        "        {\n" +
                        "            \"_source\": {\n" +
                        "                \"discoverydate\":\"2022-07-03 11:00:00+0000\",\n" +
                        "                \"_cloudType\": \"Azure\",\n" +
                        "                \"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "                \"region\":\"null\",\n" +
                        "                \"subscriptionName\": \"dev-paladincloud\",\n" +
                        "                \"resourceGroupName\": \"dev-paladincloud\",\n" +
                        "                \"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n" +
                        "                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n" +
                        "                \"canAccessFromAzureServices\":\"true\",\n" +
                        "                \"name\":\"sqlvaisndaov4vhjgg\",\n" +
                        "                \"regionName\":\"eastus\",\n" +
                        "                \"customerManagedKey\":\"null\",\n" +
                        "                \"minimumTlsVersion\":\"TLS1_1\",\n"   +
                        "                \"systemAssignedManagedServiceIdentityPrincipalId\":\"null\",\n" +
                        "                \"systemAssignedManagedServiceIdentityTenantId\":\"null\",\n" +
                        "                \"endPoints\":\"null\",\n" +
                        "                \"ipAddressesWithAccess\":[],\n" +
                        "                \"ipAddressRangesWithAccess\":[],\n" +
                        "                \"networkSubnetsWithAccess\":[],\n" +
                        "                \"tags\":{},\n" +
                        "                \"kind\":\"StorageV2\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ]\n" +
                        "\n" +
                        "}",
                JsonElement.class));
        return jsonObject;
    }

    public JsonObject getHitJsonArrayForMinTlsVersion() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\n" +
                        "    \"hits\": [\n" +
                        "        {\n" +
                        "            \"_source\": {\n" +
                        "                \"discoverydate\":\"2022-07-03 11:00:00+0000\",\n" +
                        "                \"_cloudType\": \"Azure\",\n" +
                        "                \"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "                \"region\":\"null\",\n" +
                        "                \"subscriptionName\": \"dev-paladincloud\",\n" +
                        "                \"resourceGroupName\": \"dev-paladincloud\",\n" +
                        "                \"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/dev-rg-1/providers/Microsoft.Storage/storageAccounts/sqlvaisndaov4vhjgg\",\n" +
                        "                \"key\": \"ccb7e20e-47c3-478b-a960-580c7a6b9d1e\",\n" +
                        "                \"canAccessFromAzureServices\":\"true\",\n" +
                        "                \"name\":\"sqlvaisndaov4vhjgg\",\n" +
                        "                \"regionName\":\"eastus\",\n" +
                        "                \"customerManagedKey\":\"null\",\n" +
                        "                \"minimumTlsVersion\":\"TLS1_2\",\n"   +
                        "                \"systemAssignedManagedServiceIdentityPrincipalId\":\"null\",\n" +
                        "                \"systemAssignedManagedServiceIdentityTenantId\":\"null\",\n" +
                        "                \"endPoints\":\"null\",\n" +
                        "                \"ipAddressesWithAccess\":[],\n" +
                        "                \"ipAddressRangesWithAccess\":[],\n" +
                        "                \"networkSubnetsWithAccess\":[],\n" +
                        "                \"tags\":{},\n" +
                        "                \"kind\":\"StorageV2\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ]\n" +
                        "\n" +
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
                .thenReturn(getHitJsonArrayForMinTlsVersion());
        assertThat(setLatestTlsVersionRule.execute(CommonTestUtils.getMapString("r_123 "),
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
                .thenReturn(getFailureJsonArrayForMinTlsVersion());
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(setLatestTlsVersionRule.execute(CommonTestUtils.getMapString("r_123 "),
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
        assertThat(setLatestTlsVersionRule.getHelpText(), is(notNullValue()));
    }
}
