package com.tmobile.cloud.awsrules.iam;

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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class IAMUserWithMultipleAccessKeyRuleTest {

    @InjectMocks
    IAMUserWithMultipleAccessKeyRule iamUserWithMultipleAccessKeyRule;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.getAccessKeysForIamUser(anyString(), anyString())).thenReturn(new HashSet<String>());
        assertThat(iamUserWithMultipleAccessKeyRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        Set<String> keys = new HashSet<>();
        keys.add("assdasdsadwfv");
        keys.add("ertyryuryuyjh");
        when(PacmanUtils.getAccessKeysForIamUser(anyString(), anyString())).thenReturn(keys);
        assertThat(iamUserWithMultipleAccessKeyRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.getAccessKeysForIamUser(anyString(), anyString())).thenThrow(new Exception());
        assertThatThrownBy(() -> iamUserWithMultipleAccessKeyRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> iamUserWithMultipleAccessKeyRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(iamUserWithMultipleAccessKeyRule.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "IamUserWithMultipleAccessKey_version-1_IAMUserShouldUseSingleKey_iamuser");
        commonMap.put("policyId", "IamUserWithMultipleAccessKey_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("esIamUserKeyUrl", "esIamUserKeyUrl");
        return commonMap;
    }
  
    
}
