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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
public class CheckRoute53ExpiredDomainNamesTest {

    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;

    @InjectMocks
    private CheckRoute53ExpiredDomainNames checkRoute53ExpiredDomainNames;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        ruleParam = Route53TestUtil.getInputParamMap();
        resourceAttribute = Route53TestUtil.getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {

        OffsetDateTime expiry = LocalDateTime.now().plusDays(20).atOffset(ZoneOffset.UTC);
        String expiryDate = Route53TestUtil.dateTimeFormatter.format(expiry);
        resourceAttribute.put(PacmanRuleConstants.EXPIRATION_DATE, expiryDate);
        PolicyResult ruleResult = checkRoute53ExpiredDomainNames.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestValidationFail() throws Exception {

        OffsetDateTime expiry = LocalDateTime.now().minusDays(20).atOffset(ZoneOffset.UTC);
        String expiryDate = Route53TestUtil.dateTimeFormatter.format(expiry);
        resourceAttribute.put(PacmanRuleConstants.EXPIRATION_DATE, expiryDate);
        when(PacmanUtils.doesAllHaveValue(anyString(), eq(expiryDate), anyString())).thenReturn(false);
        PolicyResult ruleResult = checkRoute53ExpiredDomainNames.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestExpired() throws Exception {

        OffsetDateTime expiry = LocalDateTime.now().minusDays(20).atOffset(ZoneOffset.UTC);
        String expiryDate = Route53TestUtil.dateTimeFormatter.format(expiry);
        resourceAttribute.put(PacmanRuleConstants.EXPIRATION_DATE, expiryDate);
        PolicyResult ruleResult = checkRoute53ExpiredDomainNames.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTestExpiryDateNull() throws Exception {

        PolicyResult ruleResult = checkRoute53ExpiredDomainNames.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkRoute53ExpiredDomainNames.getHelpText());
    }

}
