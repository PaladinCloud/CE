package com.tmobile.cloud.awsrules.route53;

import com.tmobile.cloud.awsrules.route53.util.Route53TestUtil;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
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
public class CheckRoute53DomainPrivacyProtectionTest {

    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;

    @InjectMocks
    private CheckRoute53DomainPrivacyProtection checkRoute53DomainPrivacyProtection;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        ruleParam = Route53TestUtil.getInputParamMap();
        resourceAttribute = Route53TestUtil.getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.REGISTRANT_PRIVACY, Boolean.TRUE.toString());
        PolicyResult ruleResult = checkRoute53DomainPrivacyProtection.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeFailTest() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.REGISTRANT_PRIVACY, Boolean.FALSE.toString());
        PolicyResult ruleResult = checkRoute53DomainPrivacyProtection.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestInvalidValue() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.REGISTRANT_PRIVACY, "fail");
        PolicyResult ruleResult = checkRoute53DomainPrivacyProtection.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestValidationFail() throws Exception {
        resourceAttribute.put(PacmanRuleConstants.REGISTRANT_PRIVACY, Boolean.FALSE.toString());
        when(PacmanUtils.doesAllHaveValue(anyString(), eq(Boolean.FALSE.toString()), anyString())).thenReturn(false);
        PolicyResult ruleResult = checkRoute53DomainPrivacyProtection.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkRoute53DomainPrivacyProtection.getHelpText());
    }
}
