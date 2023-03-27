package com.tmobile.cloud.awsrules.eks;

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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BasePolicy.class, Annotation.class})
public class CheckEKSClusterEncryptionEnabledRuleTest {

    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;
    @InjectMocks
    private CheckEKSClusterEncryptionEnabledRule checkEKSClusterEncryptionEnabledRule;

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

        PolicyResult ruleResult = checkEKSClusterEncryptionEnabledRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeFailTest() throws Exception {
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        resourceAttribute.remove(PacmanRuleConstants.ES_KEY_ARN_ATTRIBUTE);
        PolicyResult ruleResult = checkEKSClusterEncryptionEnabledRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkEKSClusterEncryptionEnabledRule.getHelpText());
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
        resObj.put(PacmanRuleConstants.ES_KEY_ARN_ATTRIBUTE, "arn:");
        return resObj;
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
}
