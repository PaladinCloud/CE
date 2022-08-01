package com.tmobile.cloud.awsrules.asg;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class AutoScalingGroupsWithMissingAMITest {

    @InjectMocks
    AutoScalingGroupsWithMissingAMI autoScalingGroupsWithMissingAMI;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);	
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        
        Set<String> imageSet = new HashSet<>();
        imageSet.add("ami-12345");
        when(PacmanUtils.getImagesByAsgArn(anyString(), anyString())).thenReturn(Arrays.asList("ami-12213,ami-56789"));
        when(PacmanUtils.getMissingAMIs(anySetOf(String.class), anyString())).thenReturn(imageSet);
        assertThat(autoScalingGroupsWithMissingAMI.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getImagesByAsgArn(anyString(), anyString())).thenReturn(Arrays.asList("ami-12345,ami-56789"));
        when(PacmanUtils.getMissingAMIs(anySetOf(String.class), anyString())).thenReturn(new HashSet<>());
        assertThat(autoScalingGroupsWithMissingAMI.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getImagesByAsgArn(anyString(), anyString())).thenThrow(new Exception());
        assertThatThrownBy(() -> autoScalingGroupsWithMissingAMI.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> autoScalingGroupsWithMissingAMI.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(autoScalingGroupsWithMissingAMI.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "AwsAsgInactiveAMI_version-1_AwsAsgInactiveAMI_asg");
        commonMap.put("policyId", "AwsAsgInactiveAMI_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("autoscalinggrouparn", "arn:aws:autoscaling:region:123:autoScalingGroup:34665767:autoScalingGroupName/asg-sd3242352dfdfd");
        commonMap.put("esAsgLcURL", "esAsgLcURL");
        commonMap.put("esAmiUrl", "esAmiUrl");
        return commonMap;
    }
  
    
}
