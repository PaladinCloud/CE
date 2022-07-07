package com.tmobile.cloud.awsrules.securitygroup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class PublicAccessForDefaultSecurityGroupTest {

    @InjectMocks
    PublicAccessForDefaultSecurityGroup publicAccessForDefaultSecurityGroup;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        
        when(PacmanUtils.getDefaultSecurityGroupsByName(anyString(),anyString())).thenReturn(getListSecurityGroupId());
        when(PacmanUtils.getUnrestrictedSecurityGroupsById(anySetOf(String.class),anyString(),anyString(),anyString())).thenReturn(getListSecurityGroupId());
        assertThat(publicAccessForDefaultSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getDefaultSecurityGroupsByName(anyString(),anyString())).thenReturn(getListSecurityGroupId());
        when(PacmanUtils.getUnrestrictedSecurityGroupsById(anySetOf(String.class),anyString(),anyString(),anyString())).thenReturn(new ArrayList<>());
        assertThat(publicAccessForDefaultSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getDefaultSecurityGroupsByName(anyString(),anyString())).thenThrow(new Exception());
        assertThatThrownBy( 
                () -> publicAccessForDefaultSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> publicAccessForDefaultSecurityGroup.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "critical");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "AwsPublicAccessDefaultSecurityGroup_version-1_AwsPublicAccessDefaultSecurityGroup_ec2");
        commonMap.put("policyId", "AwsPublicAccessDefaultSecurityGroup_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("securityGroupName", "default");
        commonMap.put("statename", "running");
        commonMap.put("esSgURL", "esSgURL");
        commonMap.put("esSgRulesUrl", "esSgRulesUrl");
        commonMap.put("cidrIp", "0.0.0.0/0");
        commonMap.put("cidripv6", "::/0");
        return commonMap;
    }
    
    public static List<String> getListSecurityGroupId() {
        List<String> groupIdentifiers = new ArrayList<>();
        groupIdentifiers.add("sg-12345");
        return groupIdentifiers;
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(publicAccessForDefaultSecurityGroup.getHelpText(), is(notNullValue()));
    }
}
