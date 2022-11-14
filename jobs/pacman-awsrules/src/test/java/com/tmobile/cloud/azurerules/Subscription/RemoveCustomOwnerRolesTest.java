package com.tmobile.cloud.azurerules.Subscription;

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
public class RemoveCustomOwnerRolesTest {

    @InjectMocks
    RemoveCustomOwnerRoles removeCustomOwnerRoles;

    public JsonObject getFailureForCustomOwnerRoles(){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\n" +
                        "    \"hits\": [\n" +
                        "    {\n" +
                        "    \n" +
                        "    \n" +
                        "       \"_index\": \"azure_subscription\",\n" +
                        "        \"_type\": \"subscription\",\n" +
                        "        \"_id\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "        \"_score\": 0.18232156,\n" +
                        "        \"_source\": {\n" +
                        "          \"discoverydate\": \"2022-09-19 07:00:00+0000\",\n" +
                        "          \"_cloudType\": \"Azure\",\n" +
                        "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"region\": null,\n" +
                        "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                        "          \"resourceGroupName\": null,\n" +
                        "          \"id\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"subscriptionId\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"tenant\": \"db8e92e6-bdc8-4d89-97c9-3e52cbe9d583\",\n" +
                        "          \"_resourceid\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"_docid\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"roleDefinitionList\":[\n" +
                        "          \"assignableScopes\":[\n" +
                        "          \" *** \",\n" +
                        "           ]\n" +
                        "          \"actions\":[\n" +
                        "          \"  *  \",\n"+
                        "           ]\n" +
                        "           ]\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"subscription\",\n" +
                        "          \"firstdiscoveredon\": \"2022-09-04 16:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-09-19 07:56:00+0000\"\n" +
                        "        }\n" +
                        "        }\n" +
                        "         ]\n" +
                        "\n" +
                        "}",
                JsonElement.class));
        return jsonObject;
    }

    public JsonObject getSuccessForCustomOwnerRoles() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson(
                "{\n" +
                        "    \"hits\": [\n" +
                        "    {\n" +
                        "    \n" +
                        "    \n" +
                        "       \"_index\": \"azure_subscription\",\n" +
                        "        \"_type\": \"subscription\",\n" +
                        "        \"_id\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "        \"_score\": 0.18232156,\n" +
                        "        \"_source\": {\n" +
                        "          \"discoverydate\": \"2022-09-19 07:00:00+0000\",\n" +
                        "          \"_cloudType\": \"Azure\",\n" +
                        "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"region\": null,\n" +
                        "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                        "          \"resourceGroupName\": null,\n" +
                        "          \"id\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"subscriptionId\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"tenant\": \"db8e92e6-bdc8-4d89-97c9-3e52cbe9d583\",\n" +
                        "          \"_resourceid\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"_docid\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                        "          \"roleDefinitionList\":[\n" +
                        "          \"assignableScopes\":[\n" +
                        "          \" /subscriptions/abcdabcd-1234-1234-1234-abcdabcdabcd \",\n" +
                        "           ]\n" +
                        "          \"actions\":[\n" +
                        "          \"  *  \",\n"+
                        "           ]\n" +
                        "           ]\n" +
                        "          \"_entity\": \"true\",\n" +
                        "          \"_entitytype\": \"subscription\",\n" +
                        "          \"firstdiscoveredon\": \"2022-09-04 16:00:00+0000\",\n" +
                        "          \"latest\": true,\n" +
                        "          \"_loaddate\": \"2022-09-19 07:56:00+0000\"\n" +
                        "        }\n" +
                        "        }\n" +
                        "         ]\n" +
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
                .thenReturn(getSuccessForCustomOwnerRoles());
        assertThat(removeCustomOwnerRoles.execute(CommonTestUtils.getMapString("r_123 "),
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
                .thenReturn(getFailureForCustomOwnerRoles());
        assertThat(removeCustomOwnerRoles.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void getHelpTextTest() {
        assertThat(removeCustomOwnerRoles.getHelpText(), is(notNullValue()));
    }
}
