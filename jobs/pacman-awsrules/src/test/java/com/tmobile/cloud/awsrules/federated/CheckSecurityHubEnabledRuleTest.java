package com.tmobile.cloud.awsrules.federated;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
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
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BasePolicy.class, Annotation.class})
public class CheckSecurityHubEnabledRuleTest {

    @InjectMocks
    private CheckSecurityHubEnabledRule checkSecurityHubEnabledRule;

    private static final String SECURITY_HUB_URL = "/aws/securityhub/_search";
    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        ruleParam = getInputParamMap();
        resourceAttribute = getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SECURITY_HUB_URL), any(), any(),
                any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        PolicyResult ruleResult = checkSecurityHubEnabledRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeFailTest() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SECURITY_HUB_URL), any(), any(),
                any(), any(), any())).thenReturn(null);
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        PolicyResult ruleResult = checkSecurityHubEnabledRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
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
    public void regionParamNotPresentTest() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + SECURITY_HUB_URL), any(), any(),
                any(), any(), any())).thenReturn(null);
        ruleParam.put(PacmanRuleConstants.REGIONS,"");
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        PolicyResult ruleResult = checkSecurityHubEnabledRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkSecurityHubEnabledRule.getHelpText());
    }

    private Map<String, String> getInputParamMap() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.POLICY_ID,
                "test_version-1_AWSSecurityHub_test");
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
        resObj.put(PacmanRuleConstants.NAME, "test");
        return resObj;
    }
}
