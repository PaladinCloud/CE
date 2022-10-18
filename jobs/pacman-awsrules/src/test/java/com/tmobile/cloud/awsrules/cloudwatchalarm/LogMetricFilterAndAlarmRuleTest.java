package com.tmobile.cloud.awsrules.cloudwatchalarm;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BaseRule.class })
public class LogMetricFilterAndAlarmRuleTest {

	@InjectMocks
	LogMetricFilterAndAlarmRule logMetricFilterAndAlarmRule;

	private static final String CLOUD_TRAIL_URL = "/aws/cloudtrail/_search";
	private static final String CLOUD_WATCH_LOGS_METRIC_URL = "/aws/cloudwatchlogs_metric/_search";
	private static final String CLOUD_WATCH_ALARM_URL = "/aws/cloudwatchalarm/_search";
	private static final String CT_CFG_CHANGES_FILTER =
			"{ ($.eventName = CreateTrail) || ($.eventName = UpdateTrail) || ($.eventName = DeleteTrail) "
					+ "|| ($.eventName = StartLogging) || ($.eventName = StopLogging) }";
	
	@Test
	public void executeTest() throws Exception {

		String filterName = "CT_CFG_CHANGES_FILTER";
		mockStatic(PacmanUtils.class);

		when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap(filterName);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_URL), any(), any(),
				any(), any(), any())).thenReturn(new HashSet<>(Arrays.asList("test")));
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_WATCH_LOGS_METRIC_URL),
				any(), any(), any(), any(), any())).thenReturn(new HashSet<>(Arrays.asList(CT_CFG_CHANGES_FILTER)));
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_WATCH_ALARM_URL), any(),
				any(), any(), any(), any())).thenReturn(new HashSet<>(Arrays.asList("test")));

		RuleResult ruleResult = logMetricFilterAndAlarmRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void invalidFilterNameTest() throws Exception {

		String filterName = "test";
		mockStatic(PacmanUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap(filterName);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getInValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.getValueFromElasticSearchAsSet(any(), any(), any(), any(), any(), any()))
				.thenReturn(new HashSet<>(Arrays.asList("test")));

		String expected = "Invalid value for filter, filter: " + filterName;

		RuleResult ruleResult = logMetricFilterAndAlarmRule.execute(ruleParam, resourceAttribute);
		assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void executethrowsExceptionTest() throws Exception {

		String filterName = "test";
		mockStatic(PacmanUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap(filterName);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getInValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.getValueFromElasticSearchAsSet(any(), any(), any(), any(), any(), any()))
				.thenThrow(new RuntimeException());

		String expected = "Cloudwatch alarm not found";

		RuleResult ruleResult = logMetricFilterAndAlarmRule.execute(ruleParam, resourceAttribute);
		assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void cloudTrailNotFoundTest() throws Exception {

		String filterName = "test";
		mockStatic(PacmanUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap(filterName);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getInValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.getValueFromElasticSearchAsSet(any(), any(), any(), any(), any(), any())).thenReturn(null);

		String expected = "CloudTrail log with matching conditions does not exists, isMultiRegionTrail: true"
				+ ", isLogging: true, accountId: " + resourceAttribute.get(PacmanRuleConstants.ACCOUNTID);

		RuleResult ruleResult = logMetricFilterAndAlarmRule.execute(ruleParam, resourceAttribute);
		assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void cloudTrailMetriclogNotFoundTest() throws Exception {

		String filterName = "CT_CFG_CHANGES_FILTER";
		mockStatic(PacmanUtils.class);

		when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap(filterName);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getInValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_URL), any(), any(),
				any(), any(), any())).thenReturn(new HashSet<>(Arrays.asList("test")));
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_WATCH_LOGS_METRIC_URL),
				any(), any(), any(), any(), any())).thenReturn(null);

		String expected = "Cloudwatch logs with matching conditions does not exists, metricname: "
				+ ruleParam.get(PacmanRuleConstants.METRIC_NAME) + ", metricnamespace: "
				+ ruleParam.get(PacmanRuleConstants.METRIC_NAMESPACE) + ", filtername: " + filterName + ", accountId: "
				+ resourceAttribute.get(PacmanRuleConstants.ACCOUNTID);

		RuleResult ruleResult = logMetricFilterAndAlarmRule.execute(ruleParam, resourceAttribute);
		assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void cloudTrailFilterPatternNotMatchingTest() throws Exception {

		String filterName = "CT_CFG_CHANGES_FILTER";
		mockStatic(PacmanUtils.class);

		when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap(filterName);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getInValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_URL), any(), any(),
				any(), any(), any())).thenReturn(new HashSet<>(Arrays.asList("test")));
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_WATCH_LOGS_METRIC_URL),
				any(), any(), any(), any(), any())).thenReturn(new HashSet<>(Arrays.asList("test")));

		String expected = "Cloudwatch logs with matching filter patterns does not exists, filtername: " 
				+ filterName + ", accountId: " + resourceAttribute.get(PacmanRuleConstants.ACCOUNTID);

		RuleResult ruleResult = logMetricFilterAndAlarmRule.execute(ruleParam, resourceAttribute);
		assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void cloudWatchAlarmNotFoundTest() throws Exception {

		String filterName = "CT_CFG_CHANGES_FILTER";
		mockStatic(PacmanUtils.class);

		when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap(filterName);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getInValidResourceData("test1");

		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_TRAIL_URL), any(), any(),
				any(), any(), any())).thenReturn(new HashSet<>(Arrays.asList("test")));
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_WATCH_LOGS_METRIC_URL),
				any(), any(), any(), any(), any())).thenReturn(new HashSet<>(Arrays.asList(CT_CFG_CHANGES_FILTER)));
		when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + CLOUD_WATCH_ALARM_URL), any(),
				any(), any(), any(), any())).thenReturn(null);

		String expected = "Cloudwatch alarm with matching conditions does not exists, metricname: "
				+ ruleParam.get(PacmanRuleConstants.METRIC_NAME) + ", namespace: "
				+ ruleParam.get(PacmanRuleConstants.METRIC_NAMESPACE) + ", accountId: "
				+ resourceAttribute.get(PacmanRuleConstants.ACCOUNTID);

		RuleResult ruleResult = logMetricFilterAndAlarmRule.execute(ruleParam, resourceAttribute);
		assertTrue(ruleResult.getAnnotation().get("issueDetails").contains(expected));
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);

		Map<String, String> ruleParam = getInputParamMap("test");
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");

		Map<String, String> resourceAttribute = getValidResourceData("test1");

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> logMetricFilterAndAlarmRule.execute(ruleParam, resourceAttribute))
				.isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void getHelpTest() {

		assertNotNull(logMetricFilterAndAlarmRule.getHelpText());
	}

	private Map<String, String> getInputParamMap(String filterName) {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID,
				"CloudTrailEncryption_version-1_CloudTrailWithoutEncryption_cloudtrail");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		ruleParam.put(PacmanRuleConstants.METRIC_NAME, "metricNameTest");
		ruleParam.put(PacmanRuleConstants.METRIC_NAMESPACE, "metricNmespaceTest");
		ruleParam.put(PacmanRuleConstants.FILTER_NAME, filterName);
		return ruleParam;
	}

	private Map<String, String> getValidResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		return resObj;
	}

	private Map<String, String> getInValidResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		return resObj;
	}

}
