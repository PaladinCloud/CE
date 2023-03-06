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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static com.tmobile.cloud.awsrules.route53.util.Route53TestUtil.HOSTED_ZONE_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BasePolicy.class})
public class CheckRoute53DomainDNSServiceTest {

    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;

    @InjectMocks
    private CheckRoute53DomainDNSService checkRoute53DomainDNSService;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        ruleParam = Route53TestUtil.getInputParamMap();
        resourceAttribute = Route53TestUtil.getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + HOSTED_ZONE_URL), any(), any(),
                any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        PolicyResult ruleResult = checkRoute53DomainDNSService.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeTestThrowsException() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + HOSTED_ZONE_URL), any(), any(),
                any(), any(), any())).thenThrow(new RuntimeException());
        PolicyResult ruleResult = checkRoute53DomainDNSService.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestHostedZoneNotFound() throws Exception {
        PolicyResult ruleResult = checkRoute53DomainDNSService.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkRoute53DomainDNSService.getHelpText());
    }
}
