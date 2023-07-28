package com.tmobile.cloud.azurerules.LoadBalancer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
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
@PrepareForTest({ PacmanUtils.class, BasePolicy.class, RulesElasticSearchRepositoryUtil.class, Annotation.class})
public class RemoveUnusedLoadBalancerTest {
    @InjectMocks
    RemoveUnusedLoadBalancer removeUnusedLoadBalancer ;

    public JsonObject getHitJsonArrayForUnusedLoadBalancer(){
        Gson gson=new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\":[{\"_source\":{\"discoverydate\":\"2023-07-18 10:00:00+0000\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":\"eastus\",\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"MC_dev-paladincloud_demo_eastus\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_demo_eastus/providers/Microsoft.Network/loadBalancers/kubernetes\",\"hashCode\":250964371,\"name\":\"kubernetes\",\"key\":\"d6c84710-c441-4dc0-8465-14c906ddac43\",\"refresh\":null,\"regionName\":\"eastus\",\"type\":\"Microsoft.Network/loadBalancers\",\"publicIPAddressIds\":[\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_demo_eastus/providers/Microsoft.Network/publicIPAddresses/106fa0e4-3eab-4e2d-aa35-6964d8bb433d\"],\"tags\":{\"aks-managed-cluster-name\":\"demo\",\"Environment\":\"dev\",\"aks-managed-cluster-rg\":\"dev-paladincloud\",\"Application\":\"paladincloud\"},\"backendPoolInstances\":[\"ipConfig1\"],\"loadBalancingRules\":null,\"privateFrontends\":null,\"publicFrontends\":null,\"_resourcename\":\"kubernetes\",\"_resourceid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_demo_eastus/providers/Microsoft.Network/loadBalancers/kubernetes\",\"_docid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_demo_eastus/providers/Microsoft.Network/loadBalancers/kubernetes\",\"_entity\":\"true\",\"_entitytype\":\"loadbalancer\",\"targettypedisplayname\":\"Load Balancer\",\"accountname\":\"dev-paladincloud\",\"accountid\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"docType\":\"loadbalancer\",\"loadbalancer_relations\":\"loadbalancer\",\"firstdiscoveredon\":\"2023-06-14 11:00:00+0000\",\"latest\":true,\"_loaddate\":\"2023-07-18 10:32:00+0000\"}}]}", JsonElement.class));
        return jsonObject;
    }
    public  JsonObject getFailureJsonArrayForUnusedLoadBalancer() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("hits", gson.fromJson("{\"hits\":[{\"_source\":{\"discoverydate\":\"2023-07-18 10:00:00+0000\",\"_cloudType\":\"Azure\",\"subscription\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"region\":\"eastus\",\"subscriptionName\":\"dev-paladincloud\",\"resourceGroupName\":\"MC_dev-paladincloud_demo_eastus\",\"id\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_demo_eastus/providers/Microsoft.Network/loadBalancers/kubernetes\",\"hashCode\":250964371,\"name\":\"kubernetes\",\"key\":\"d6c84710-c441-4dc0-8465-14c906ddac43\",\"refresh\":null,\"regionName\":\"eastus\",\"type\":\"Microsoft.Network/loadBalancers\",\"publicIPAddressIds\":[\"/subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_demo_eastus/providers/Microsoft.Network/publicIPAddresses/106fa0e4-3eab-4e2d-aa35-6964d8bb433d\"],\"tags\":{\"aks-managed-cluster-name\":\"demo\",\"Environment\":\"dev\",\"aks-managed-cluster-rg\":\"dev-paladincloud\",\"Application\":\"paladincloud\"},\"backendPoolInstances\":[],\"loadBalancingRules\":null,\"privateFrontends\":null,\"publicFrontends\":null,\"_resourcename\":\"kubernetes\",\"_resourceid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_demo_eastus/providers/Microsoft.Network/loadBalancers/kubernetes\",\"_docid\":\"subscriptions/f4d319d8-7eac-4e15-a561-400f7744aa81/resourceGroups/MC_dev-paladincloud_demo_eastus/providers/Microsoft.Network/loadBalancers/kubernetes\",\"_entity\":\"true\",\"_entitytype\":\"loadbalancer\",\"targettypedisplayname\":\"Load Balancer\",\"accountname\":\"dev-paladincloud\",\"accountid\":\"f4d319d8-7eac-4e15-a561-400f7744aa81\",\"docType\":\"loadbalancer\",\"loadbalancer_relations\":\"loadbalancer\",\"firstdiscoveredon\":\"2023-06-14 11:00:00+0000\",\"latest\":true,\"_loaddate\":\"2023-07-18 10:32:00+0000\"}}]}", JsonElement.class));
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getHitJsonArrayForUnusedLoadBalancer());
        assertThat(removeUnusedLoadBalancer.execute(CommonTestUtils.getMapString("r_123 "),
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
                anyObject(), anyObject(), anyInt(), anyObject(), anyObject(), anyObject())).thenReturn(getFailureJsonArrayForUnusedLoadBalancer());
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(removeUnusedLoadBalancer.execute(CommonTestUtils.getMapString("r_123 "),
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
        assertThat(removeUnusedLoadBalancer.getHelpText(), is(notNullValue()));
    }

}
