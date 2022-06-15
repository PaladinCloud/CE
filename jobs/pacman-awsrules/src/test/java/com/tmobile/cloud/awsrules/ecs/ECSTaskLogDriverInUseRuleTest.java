package com.tmobile.cloud.awsrules.ecs;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class ECSTaskLogDriverInUseRuleTest {

    @InjectMocks
    ECSTaskLogDriverInUseRule ecsTaskLogDriverInUseRule;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(true);
        
        assertThat(ecsTaskLogDriverInUseRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        assertThat(ecsTaskLogDriverInUseRule.execute(getMapNotExistingString("r_123 "),getMapNotExistingString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> ecsTaskLogDriverInUseRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(ecsTaskLogDriverInUseRule.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Aws_ecs_task_should_use_log_driver_version-1_aws_enable_task_log_driver_ecs");
        commonMap.put("policyId", "Aws_ecs_task_should_use_log_driver_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("logdriver", "awslogs");
        return commonMap;
    }
    public static Map<String, String> getMapNotExistingString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Aws_ecs_task_should_use_log_driver_version-1_aws_enable_task_log_driver_ecs");
        commonMap.put("policyId", "Aws_ecs_task_should_use_log_driver_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("logdriver", "None");
        return commonMap;
    }
  
    
}
