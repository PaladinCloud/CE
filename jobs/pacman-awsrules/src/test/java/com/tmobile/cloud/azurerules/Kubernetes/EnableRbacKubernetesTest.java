package com.tmobile.cloud.azurerules.Kubernetes;

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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class, RulesElasticSearchRepositoryUtil.class})
public class EnableRbacKubernetesTest {
    @InjectMocks
    KubernetesRbacRule kubernetesRbacRule;
    public JsonObject getFailureJsonArrayForKubernetesRbac(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n" +
                "    \"total\": 1,\n" +
                "    \"max_score\": 1,\n" +
                "    \"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"azure_kubernetes\",\n" +
                "        \"_type\": \"kubernetes\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/demo\",\n" +
                "        \"_score\": 1,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-10-27 19:00:00+0530\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/demo\",\n" +
                "          \"enableRBAC\": false,\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/demo\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/demo\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"kubernetes\",\n" +
                "          \"firstdiscoveredon\": \"2022-10-27 19:00:00+0530\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-10-27 14:49:00+0000\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }", JsonElement.class));
        return jsonObject;
    }

    public  JsonObject getHitJsonArrayForKubernetesRbac() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\n" +
                "    \"total\": 1,\n" +
                "    \"max_score\": 1,\n" +
                "    \"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"azure_kubernetes\",\n" +
                "        \"_type\": \"kubernetes\",\n" +
                "        \"_id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/demo\",\n" +
                "        \"_score\": 1,\n" +
                "        \"_source\": {\n" +
                "          \"discoverydate\": \"2022-10-27 19:00:00+0530\",\n" +
                "          \"_cloudType\": \"Azure\",\n" +
                "          \"subscription\": \"f4d319d8-7eac-4e15-a561-400f7744aa81\",\n" +
                "          \"region\": null,\n" +
                "          \"subscriptionName\": \"dev-paladincloud\",\n" +
                "          \"resourceGroupName\": null,\n" +
                "          \"id\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/demo\",\n" +
                "          \"enableRBAC\": true,\n" +
                "          \"_resourceid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/demo\",\n" +
                "          \"_docid\": \"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourcegroups/dev-paladincloud/providers/Microsoft.ContainerService/managedClusters/demo\",\n" +
                "          \"_entity\": \"true\",\n" +
                "          \"_entitytype\": \"kubernetes\",\n" +
                "          \"firstdiscoveredon\": \"2022-10-27 19:00:00+0530\",\n" +
                "          \"latest\": true,\n" +
                "          \"_loaddate\": \"2022-10-27 14:49:00+0000\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }", JsonElement.class));
        return jsonObject;
    }

    @Test
    public void executeSucessForSetAllUsersToOwnerTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForKubernetesRbac());
        assertThat(kubernetesRbacRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeFailureForNotifyAlertsTest() throws Exception {
        mockStatic(PacmanUtils.class);
        mockStatic(RulesElasticSearchRepositoryUtil.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString()))
                .thenReturn(
                        true);
        when(RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(anyString(),anyObject(),
                anyObject(),
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForKubernetesRbac());
        assertThat(kubernetesRbacRule.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")).getStatus(), is(PacmanSdkConstants.STATUS_FAILURE));
    }
}


