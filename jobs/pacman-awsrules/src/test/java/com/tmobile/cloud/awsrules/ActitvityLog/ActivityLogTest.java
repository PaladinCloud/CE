package com.tmobile.cloud.awsrules.ActitvityLog;

import com.amazonaws.services.identitymanagement.model.InvalidInputException;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.ActivityLog.ActivityLogRule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class })
public class ActivityLogTest {
    @InjectMocks
    ActivityLogRule activityLogRule;

    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        assertThat(activityLogRule.execute(getMapNotExistString("r_123 "), getMapNotExistString("r_123 ")),
                is(notNullValue()));

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");

        when(PacmanUtils.checkISResourceIdExistsFromElasticSearch("_resourceid", "azure_activitylog/_search", "", ""))
                .thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> activityLogRule.execute(getMapString("r_123 "), getMapString("r_123 ")))
                .isInstanceOf(InvalidInputException.class);

    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("ruleId",
                "PacMan_Enable_Azure_Account_Create_Policy_Assignment_log_alert");
        commonMap.put("policyId", "PacMan_Enable_Azure_Account_Create_Policy_Assignment_log_alert");
        commonMap.put("policyVersion", "version-1");
        return commonMap;
    }

    public static Map<String, String> getMapNotExistString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("ruleId",
                "PacMan_Enable_Azure_Account_Create_Policy_Assignment_log_alert");
        commonMap.put("policyId", "PacMan_Enable_Azure_Account_Create_Policy_Assignment_log_alert");
        commonMap.put("policyVersion", "version-1");
        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(activityLogRule.getHelpText(), is(notNullValue()));
    }

}
