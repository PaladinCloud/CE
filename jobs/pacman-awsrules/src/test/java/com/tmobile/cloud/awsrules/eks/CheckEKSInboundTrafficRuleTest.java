package com.tmobile.cloud.awsrules.eks;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BasePolicy.class})
public class CheckEKSInboundTrafficRuleTest {


    private static final String SG_RULES_URL = "/aws/sg_rules/_search";
    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;
    @InjectMocks
    private CheckEKSInboundTrafficRule checkEKSInboundTrafficRule;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        ruleParam = getInputParamMap();
        resourceAttribute = getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {

        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("443")));
        PolicyResult PolicyResult = checkEKSInboundTrafficRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, PolicyResult.getStatus());
    }

    @Test
    public void executeFromPortTest() throws Exception {

        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), eq(PacmanRuleConstants.ES_SG_FROM_PORT_ATTRIBUTE),
                any())).thenReturn(new HashSet<>(Collections.singletonList("443")));
        PolicyResult PolicyResult = checkEKSInboundTrafficRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, PolicyResult.getStatus());
    }

    @Test
    public void executeToPortTest() throws Exception {

        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), eq(PacmanRuleConstants.ES_SG_TO_PORT_ATTRIBUTE),
                any())).thenReturn(new HashSet<>(Collections.singletonList("443")));
        PolicyResult PolicyResult = checkEKSInboundTrafficRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, PolicyResult.getStatus());
    }

    @Test
    public void executeFromPortAndToPortTest() throws Exception {

        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), eq(PacmanRuleConstants.ES_SG_FROM_PORT_ATTRIBUTE),
                any())).thenReturn(new HashSet<>(Collections.singletonList("443")));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), eq(PacmanRuleConstants.ES_SG_TO_PORT_ATTRIBUTE),
                any())).thenReturn(new HashSet<>(Collections.singletonList("443")));
        PolicyResult PolicyResult = checkEKSInboundTrafficRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, PolicyResult.getStatus());
    }

    @Test
    public void executeFromPortAndToPortFailTest() throws Exception {

        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), eq(PacmanRuleConstants.ES_SG_FROM_PORT_ATTRIBUTE),
                any())).thenReturn(new HashSet<>(Collections.singletonList("22")));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), eq(PacmanRuleConstants.ES_SG_TO_PORT_ATTRIBUTE),
                any())).thenReturn(new HashSet<>(Collections.singletonList("22")));
        PolicyResult PolicyResult = checkEKSInboundTrafficRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, PolicyResult.getStatus());
    }

    @Test
    public void executeFromPortFailTest() throws Exception {

        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), eq(PacmanRuleConstants.ES_SG_FROM_PORT_ATTRIBUTE),
                any())).thenReturn(new HashSet<>(Collections.singletonList("22")));
        PolicyResult PolicyResult = checkEKSInboundTrafficRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, PolicyResult.getStatus());
    }

    @Test
    public void executeToPortFailTest() throws Exception {

        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SG_RULES_URL), any(), any(),
                any(), eq(PacmanRuleConstants.ES_SG_TO_PORT_ATTRIBUTE),
                any())).thenReturn(new HashSet<>(Collections.singletonList("22")));
        PolicyResult PolicyResult = checkEKSInboundTrafficRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, PolicyResult.getStatus());
    }

    @Test
    public void executeFailTest() {

        resourceAttribute.remove(PacmanRuleConstants.CLUSTER_SECURITY_GROUP_ID);
        PolicyResult PolicyResult = checkEKSInboundTrafficRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, PolicyResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkEKSInboundTrafficRule.getHelpText());
    }

    private Map<String, String> getInputParamMap() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.POLICY_ID,
                "test_version-1_EksCluster_test");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        ruleParam.put(PacmanRuleConstants.REGIONS, "test");
        return ruleParam;
    }

    private Map<String, String> getValidResourceData() {
        Map<String, String> resObj = new HashMap<>();
        resObj.put("_resourceid", "test");
        resObj.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        resObj.put(PacmanRuleConstants.CLUSTER_NAME, "test");
        resObj.put(PacmanRuleConstants.CLUSTER_SECURITY_GROUP_ID, "sg-test");
        return resObj;
    }
}
