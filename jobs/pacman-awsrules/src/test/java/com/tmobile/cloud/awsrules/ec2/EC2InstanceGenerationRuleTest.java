package com.tmobile.cloud.awsrules.ec2;

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
public class EC2InstanceGenerationRuleTest {

    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;
    @InjectMocks
    private CheckEC2InstanceGenerationRule checkEC2InstanceGenerationRule;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        ruleParam = getInputParamMap();
        resourceAttribute = getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "t9.micro");
        PolicyResult policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, policyResult.getStatus());
    }

    @Test
    public void executePreviousGenerationTest() throws Exception {
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "t1.test");
        PolicyResult policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "i2.test");
        policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "m1.test");
        policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "m2.test");
        policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "c1.test");
        policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "hs1.test");
        policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "g2.test");
        policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
        resourceAttribute.put(PacmanRuleConstants.INSTANCE_TYPE, "test.test");
        policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
    }

    @Test
    public void executeInstanceTypeWrongTest() throws Exception {
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        PolicyResult policyResult = checkEC2InstanceGenerationRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, policyResult.getStatus());
    }


    @Test
    public void getHelpTest() {
        assertNotNull(checkEC2InstanceGenerationRule.getHelpText());
    }

    private Map<String, String> getInputParamMap() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.POLICY_ID, "test_version-1_test");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        ruleParam.put(PacmanRuleConstants.REGIONS, "test");
        ruleParam.put(PacmanRuleConstants.OLD_VERSIONS, "t1.test,i2.test,m1.test,c1.test,m2.test,hs1.test,g2.test," +
                "test.test");
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
