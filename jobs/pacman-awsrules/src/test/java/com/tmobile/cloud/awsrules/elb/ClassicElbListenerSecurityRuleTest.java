package com.tmobile.cloud.awsrules.elb;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanEc2Utils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class,PacmanEc2Utils.class})
public class ClassicElbListenerSecurityRuleTest {

    @InjectMocks
    ClassicElbListenerSecurityRule classicElbListenerSecurityRule;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(PacmanUtils.checkClassicElbUsingSecuredProtocol(anyString(),anyString(),anyString(),anyString())).thenReturn(geValidSetString("r_123 "));
        assertThat(classicElbListenerSecurityRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.checkClassicElbUsingSecuredProtocol(anyString(),anyString(),anyString(),anyString())).thenReturn(new HashSet<>());
        assertThat(classicElbListenerSecurityRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        
        when(PacmanUtils.checkClassicElbUsingSecuredProtocol(anyString(),anyString(),anyString(),anyString())).thenThrow(new Exception());
        assertThatThrownBy( 
                () -> classicElbListenerSecurityRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> classicElbListenerSecurityRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(classicElbListenerSecurityRule.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "AwsClassicElbListenerSecurity_version-1_AwsListenerSecurity_classicelb");
        commonMap.put("policyId", "AwsClassicElbListenerSecurity_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("securityGroupName", "default");
        commonMap.put("statename", "running");
        commonMap.put("esClassicELBListenerURL", "esClassicELBListenerURL");
        return commonMap;
    }
    
    public static Set<String> geValidSetString(String passRuleResourceId) {
        Set<String> commonSet = new HashSet<>();
        commonSet.add("HTTPS");
        return commonSet;
    }
}
