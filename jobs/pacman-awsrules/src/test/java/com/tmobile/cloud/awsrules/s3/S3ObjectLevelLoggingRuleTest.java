package com.tmobile.cloud.awsrules.s3;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BaseRule.class})
public class S3ObjectLevelLoggingRuleTest {

    @InjectMocks
    S3ObjectLevelReadLoggingRule s3ObjectLevelReadLoggingRule;

    @InjectMocks
    S3ObjectLevelWriteLoggingRule s3ObjectLevelWriteLoggingRule;

    private static final String CLOUD_TRAIL_URL = "/aws/cloudtrail/_search";
    private static final String CLOUD_TRAIL_EVENT_SELECTOR_URL = "/aws/cloudtrail_eventselector/_search";
    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        ruleParam = getInputParamMap();
        resourceAttribute = getValidResourceData();
    }

    @Test
    public void executeReadTest() throws Exception {

        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");
        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL), any(), any(),
                any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL),
                any(), any(), any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("All")));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_URL), any(),
                any(), any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));

        RuleResult ruleResult = s3ObjectLevelReadLoggingRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeWriteTest() throws Exception {
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL), any(), any(),
                any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL),
                any(), any(), any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("All")));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_URL), any(),
                any(), any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));

        RuleResult ruleResult = s3ObjectLevelWriteLoggingRule.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void trailEventsNotFoundTest() throws Exception {
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL), any(), any(),
                any(), any(), any())).thenReturn(null);
        String expected = "CloudTrail log with matching conditions does not exists, accountId: "
                + resourceAttribute.get(PacmanRuleConstants.ACCOUNTID)
                + " for s3 bucket: " + resourceAttribute.get(PacmanRuleConstants.NAME);

        RuleResult ruleResult = s3ObjectLevelWriteLoggingRule.execute(ruleParam, resourceAttribute);
        assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
        ruleResult = s3ObjectLevelReadLoggingRule.execute(ruleParam, resourceAttribute);
        assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void wrongTypeTest() throws Exception {
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");
        String readTypeFromSearch = "test";

        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL), any(), any(),
                any(), eq("trailarn"), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL),
                any(), any(), any(), eq("readwritetype"), any())).thenReturn(new HashSet<>(Collections.singletonList(readTypeFromSearch)));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_URL), any(),
                any(), any(), any(), any())).thenReturn(null);
        String expected = "CloudTrail log with matching conditions does not exists," +
                " readwritetype: " + readTypeFromSearch + ", accountId: "
                + resourceAttribute.get(PacmanRuleConstants.ACCOUNTID)
                + " for s3 bucket: " + resourceAttribute.get(PacmanRuleConstants.NAME);

        RuleResult ruleResult = s3ObjectLevelWriteLoggingRule.execute(ruleParam, resourceAttribute);
        assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
        ruleResult = s3ObjectLevelReadLoggingRule.execute(ruleParam, resourceAttribute);
        assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void multiRegionTrailNotFoundTest() throws Exception {
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");
        String readTypeFromSearch = "All";

        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL), any(), any(),
                any(), eq("trailarn"), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL),
                any(), any(), any(), eq("readwritetype"), any())).thenReturn(new HashSet<>(Collections.singletonList(readTypeFromSearch)));
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_URL), any(),
                any(), any(), any(), any())).thenReturn(null);
        String expected = "CloudTrail log with matching conditions does not exists, isMultiRegionTrail: true, accountId: "
                + resourceAttribute.get(PacmanRuleConstants.ACCOUNTID)
                + " for s3 bucket: " + resourceAttribute.get(PacmanRuleConstants.NAME);

        RuleResult ruleResult = s3ObjectLevelWriteLoggingRule.execute(ruleParam, resourceAttribute);
        assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
        ruleResult = s3ObjectLevelReadLoggingRule.execute(ruleParam, resourceAttribute);
        assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeThrowsTest() throws Exception {
        ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

        when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_EVENT_SELECTOR_URL), any(), any(),
                any(), eq("trailarn"), any())).thenThrow(new RuntimeException("test"));
        String expectedForWrite = "Object-level logging for write events is enabled for S3 bucket";
        String expectedForRead = "Object-level logging for read events is enabled for S3 bucket";

        RuleResult ruleResult = s3ObjectLevelWriteLoggingRule.execute(ruleParam, resourceAttribute);
        assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expectedForWrite));
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
        ruleResult = s3ObjectLevelReadLoggingRule.execute(ruleParam, resourceAttribute);
        assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expectedForRead));
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpReadTest() {

        assertNotNull(s3ObjectLevelReadLoggingRule.getHelpText());
    }

    @Test
    public void getHelpWriteTest() {

        assertNotNull(s3ObjectLevelWriteLoggingRule.getHelpText());
    }

    private Map<String, String> getInputParamMap() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.RULE_ID,
                "test_version-1_CloudTrailTest_cloudtrail");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        ruleParam.put(PacmanRuleConstants.DATA_RESOURCE_TYPE, "test");
        ruleParam.put(PacmanRuleConstants.DATA_RESOURCE_VALUE, "test");
        return ruleParam;
    }

    private Map<String, String> getValidResourceData() {
        Map<String, String> resObj = new HashMap<>();
        resObj.put("_resourceid", "test1");
        resObj.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        resObj.put(PacmanRuleConstants.NAME, "test");
        ;
        return resObj;
    }
}
