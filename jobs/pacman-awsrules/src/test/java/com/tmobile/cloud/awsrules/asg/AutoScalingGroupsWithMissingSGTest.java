package com.tmobile.cloud.awsrules.asg;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class AutoScalingGroupsWithMissingSGTest {

    @InjectMocks
    AutoScalingGroupsWithMissingSG autoScalingGroupsWithInactiveSG;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.getAsgSecurityGroupsByArn(anyString(), anyString())).thenReturn(Arrays.asList("sg-12345,sg-56789"));
        when(PacmanUtils.getInactiveSecurityGroups(anySetOf(String.class), anyString())).thenReturn(Arrays.asList("sg-12345"));
        assertThat(autoScalingGroupsWithInactiveSG.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getAsgSecurityGroupsByArn(anyString(), anyString())).thenReturn(Arrays.asList("sg-12345,sg-56789"));
        when(PacmanUtils.getInactiveSecurityGroups(anySetOf(String.class), anyString())).thenReturn(new ArrayList<>());
        assertThat(autoScalingGroupsWithInactiveSG.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getAsgSecurityGroupsByArn(anyString(), anyString())).thenThrow(new Exception());
        assertThatThrownBy(() -> autoScalingGroupsWithInactiveSG.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> autoScalingGroupsWithInactiveSG.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(autoScalingGroupsWithInactiveSG.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "AwsAsgInactiveSecurityGroup_version-1_AwsAsgInactiveSecurityGroup_asg");
        commonMap.put("policyId", "AwsAsgInactiveSecurityGroup_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("autoscalinggrouparn", "arn:aws:autoscaling:region:123:autoScalingGroup:34665767:autoScalingGroupName/asg-sd3242352dfdfd");
        commonMap.put("esAsgLcURL", "esAsgLcURL");
        commonMap.put("esSgUrl", "esSgUrl");
        return commonMap;
    }
  
    
}
