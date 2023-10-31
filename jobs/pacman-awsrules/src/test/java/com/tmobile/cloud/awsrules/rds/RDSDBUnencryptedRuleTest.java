package com.tmobile.cloud.awsrules.rds;

import com.tmobile.cloud.awsrules.utils.PacmanEc2Utils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, PacmanEc2Utils.class, Annotation.class})
public class RDSDBUnencryptedRuleTest {

    @InjectMocks
    RDSDBUnencryptedRule rdsdbUnencryptedRule;

    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(
                true);
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
        assertThat(rdsdbUnencryptedRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        assertThat(rdsdbUnencryptedRule.execute(getMapStringEncrypted("r_123 "),getMapStringEncrypted("r_123 ")), is(notNullValue()));
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(
                false);
        assertThatThrownBy(
                () -> rdsdbUnencryptedRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }

    @Test
    public void getHelpTextTest(){
        assertThat(rdsdbUnencryptedRule.getHelpText(), is(notNullValue()));
    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "PacMan_AWSRdsUnencryptedPublicInstances_version-1_AwsRdsUnencryptedPublicAccess_rdsdb");
        commonMap.put("policyId", "PacMan_AWSRdsUnencryptedPublicInstances_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("storageencrypted", "false");
        commonMap.put("publiclyaccessible", "true");
        commonMap.put("dbinstanceidentifier", "dbinstanceidentifier");
        return commonMap;
    }
    public static Map<String, String> getMapStringEncrypted(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "PacMan_AWSRdsUnencryptedPublicInstances_version-1_AwsRdsUnencryptedPublicAccess_rdsdb");
        commonMap.put("policyId", "PacMan_AWSRdsUnencryptedPublicInstances_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("storageencrypted", "true");
        commonMap.put("publiclyaccessible", "true");
        commonMap.put("dbinstanceidentifier", "dbinstanceidentifier");
        return commonMap;
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
