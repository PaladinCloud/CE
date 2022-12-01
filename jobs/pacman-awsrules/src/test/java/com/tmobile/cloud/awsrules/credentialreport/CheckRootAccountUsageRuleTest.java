package com.tmobile.cloud.awsrules.credentialreport;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class})
public class CheckRootAccountUsageRuleTest {

    private static final String CREDENTIAL_REPORT_URL = "/aws/credentialreport/_search";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    @InjectMocks
    CheckRootAccountUsageRule checkRootAccountUsageRule;

    @Test
    public void executeTest() throws Exception {

        mockStatic(PacmanUtils.class);

        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

        Map<String, String> ruleParam = getInputParamMap();
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

        Map<String, String> resourceAttribute = getValidResourceData();

        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CREDENTIAL_REPORT_URL), any(), any(),
                any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));

        RuleResult ruleResult = checkRootAccountUsageRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeConditionsSuccessTest() throws Exception {

        mockStatic(PacmanUtils.class);

        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

        Map<String, String> ruleParam = getInputParamMap();
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

        Map<String, String> resourceAttribute = getValidResourceData();

        OffsetDateTime lastUsedLocalDate = LocalDateTime.now().minusDays(20).atOffset(ZoneOffset.UTC);
        String lastUsed = dateTimeFormatter.format(lastUsedLocalDate);

        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CREDENTIAL_REPORT_URL), any(), any(),
                any(), eq("password_last_used"), any())).thenReturn(new HashSet<>(Collections.singletonList(lastUsed)));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CREDENTIAL_REPORT_URL), any(), any(),
                any(), eq("access_key_1_last_used_date"), any())).thenReturn(new HashSet<>(Collections.singletonList(lastUsed)));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CREDENTIAL_REPORT_URL), any(), any(),
                any(), eq("access_key_2_last_used_date"), any())).thenReturn(new HashSet<>(Collections.singletonList(lastUsed)));

        RuleResult ruleResult = checkRootAccountUsageRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeFailTest() throws Exception {

        mockStatic(PacmanUtils.class);

        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

        Map<String, String> ruleParam = getInputParamMap();
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

        Map<String, String> resourceAttribute = getValidResourceData();

        OffsetDateTime lastUsedLocalDate = LocalDateTime.now().minusDays(5).atOffset(ZoneOffset.UTC);
        String lastUsed = dateTimeFormatter.format(lastUsedLocalDate);

        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CREDENTIAL_REPORT_URL), any(), any(),
                any(), eq("password_last_used"), any())).thenReturn(new HashSet<>(Collections.singletonList(lastUsed)));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CREDENTIAL_REPORT_URL), any(), any(),
                any(), eq("access_key_1_last_used_date"), any())).thenReturn(new HashSet<>(Collections.singletonList(lastUsed)));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CREDENTIAL_REPORT_URL), any(), any(),
                any(), eq("access_key_2_last_used_date"), any())).thenReturn(new HashSet<>(Collections.singletonList(lastUsed)));

        RuleResult ruleResult = checkRootAccountUsageRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkRootAccountUsageRule.getHelpText());
    }

    private Map<String, String> getInputParamMap() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.RULE_ID,
                "CloudTrailEncryption_version-1_CloudTrailWithoutEncryption_cloudtrail");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        ruleParam.put(PacmanRuleConstants.MAX_DAYS, "20");
        return ruleParam;
    }

    private Map<String, String> getValidResourceData() {
        Map<String, String> resObj = new HashMap<>();
        resObj.put("_resourceid", "test1");
        resObj.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        return resObj;
    }
}
