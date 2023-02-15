package com.tmobile.cloud.awsrules.route53;

import com.tmobile.cloud.awsrules.route53.util.Route53TestUtil;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BasePolicy.class})
public class CheckRoute53DomainTransferLockTest {

    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;

    @InjectMocks
    private CheckRoute53DomainTransferLock checkRoute53DomainTransferLock;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        ruleParam = Route53TestUtil.getInputParamMap();
        resourceAttribute = Route53TestUtil.getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.STATUS_LIST, "clientTransferProhibited");
        PolicyResult ruleResult = checkRoute53DomainTransferLock.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeFailTest() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.STATUS_LIST, "test,test2");
        PolicyResult ruleResult = checkRoute53DomainTransferLock.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestStatusListEmpty() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.STATUS_LIST, StringUtils.EMPTY);
        PolicyResult ruleResult = checkRoute53DomainTransferLock.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestInvalidValue() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.STATUS_LIST, "test,test2");
        PolicyResult ruleResult = checkRoute53DomainTransferLock.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestValidationFail() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.STATUS_LIST, "test,test2");
        when(PacmanUtils.doesAllHaveValue(anyString(), eq("test,test2"), anyString())).thenReturn(false);
        PolicyResult ruleResult = checkRoute53DomainTransferLock.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkRoute53DomainTransferLock.getHelpText());
    }
}
