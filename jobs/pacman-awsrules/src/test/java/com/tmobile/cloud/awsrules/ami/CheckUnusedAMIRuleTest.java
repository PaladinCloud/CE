package com.tmobile.cloud.awsrules.ami;

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
public class CheckUnusedAMIRuleTest {

    private static final String EC2_URL = "/aws/ec2/_search";
    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;
    @InjectMocks
    private CheckUnusedAMIRule checkUnusedAMIRule;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        ruleParam = getInputParamMap();
        resourceAttribute = getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {

        resourceAttribute.put(PacmanRuleConstants.PUBLIC_VALUE, "true");
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + EC2_URL), any(),
                any(), any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        PolicyResult ruleResult = checkUnusedAMIRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executePublicValueTrueTest() throws Exception {

        resourceAttribute.put(PacmanRuleConstants.PUBLIC_VALUE, "true");
        PolicyResult policyResult = checkUnusedAMIRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, policyResult.getStatus());
    }

    @Test
    public void executeWrongPublicValueTest() throws Exception {

        resourceAttribute.put(PacmanRuleConstants.PUBLIC_VALUE, "test");
        PolicyResult policyResult = checkUnusedAMIRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
        resourceAttribute.put(PacmanRuleConstants.PUBLIC_VALUE, "false");
        policyResult = checkUnusedAMIRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
    }

    @Test
    public void executeInstanceNotFoundTest() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.PUBLIC_VALUE, "false");
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + EC2_URL), any(),
                any(), any(), any(), any())).thenReturn(null);
        PolicyResult policyResult = checkUnusedAMIRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkUnusedAMIRule.getHelpText());
    }

    private Map<String, String> getInputParamMap() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.POLICY_ID,
                "test_version-1_Ami_test");
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
        resObj.put(PacmanRuleConstants.ES_IMAGE_ID_ATTRIBUTE, "ami-test");
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");
        return resObj;
    }
}
