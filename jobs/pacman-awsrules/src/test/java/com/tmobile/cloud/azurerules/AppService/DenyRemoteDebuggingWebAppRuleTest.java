package com.tmobile.cloud.azurerules.AppService;


import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.RuleResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class })
public class DenyRemoteDebuggingWebAppRuleTest {
    @InjectMocks
    DenyRemoteDebuggingWebAppRule denyRemoteDebuggingWebAppRule;

    @Test
    public void getHelpTextTest() {
        assertThat(denyRemoteDebuggingWebAppRule.getHelpText(), is(notNullValue()));
    }

    @Test
    public void appServiceRemoteDebuggingEnabled() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.RULE_ID, "denyRemoteDebuggingWebAppRule");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        Map<String, String> resourceAttribute = getResourceForRemoteDebuggingEnabled("EMR1234");
        RuleResult ruleResult = denyRemoteDebuggingWebAppRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void appServiceRemoteDebuggingDisabled() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.RULE_ID, "denyRemoteDebuggingWebAppRule");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        Map<String, String> resourceAttribute = getResourceForRemoteDebuggingDisabled("EMR1234");
        RuleResult ruleResult = denyRemoteDebuggingWebAppRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }
    private Map<String, String> getResourceForRemoteDebuggingEnabled(String clusterID) {
        Map<String, String> clusterObj = new HashMap<>();
        clusterObj.put("_resourceid", clusterID);
        clusterObj.put("name", "AZURE-EMR");
        clusterObj.put("creationTimestamp", "2022-05-31T13:00:38.628-08:00");
        clusterObj.put("securityconfig", "securityconfig");

        return clusterObj;
    }

    private Map<String, String> getResourceForRemoteDebuggingDisabled(String clusterID) {
        Map<String, String> clusterObj = new HashMap<>();
        clusterObj.put("_resourceid", clusterID);
        clusterObj.put("name", "AZURE-EMR");
        clusterObj.put("creationTimestamp", "2022-05-31T13:00:38.628-08:00");
        clusterObj.put("securityconfig", null);

        return clusterObj;
    }
}
