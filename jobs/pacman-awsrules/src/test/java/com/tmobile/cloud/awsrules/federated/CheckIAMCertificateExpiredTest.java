package com.tmobile.cloud.awsrules.federated;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
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
import com.tmobile.pacman.commons.rule.RuleResult;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class })
public class CheckIAMCertificateExpiredTest {

	@InjectMocks
	CheckIAMCertificateExpired checkIAMCertificateExpired;

	@Test
	public void validIAMCertificate() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "Expired_AWS_IAMCertificate_version-1_Expired_IAMCertificate_iamcertificate");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "133254");
		Map<String, String> resourceAttribute = getValidIamCertificate("133254");
		RuleResult ruleResult = checkIAMCertificateExpired.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void expiredIAMCertificate() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "Expired_AWS_IAMCertificate_version-1_Expired_IAMCertificate_iamcertificate");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "3254366");
		Map<String, String> resourceAttribute = getExpiredIamCertificate("3254366");
		RuleResult ruleResult = checkIAMCertificateExpired.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void requiredDataCheck() {
		
		mockStatic(PacmanUtils.class);
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "Expired_AWS_IAMCertificate_version-1_Expired_IAMCertificate_iamcertificate");
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "12143");
		Map<String, String> resourceAttribute = getValidIamCertificate("12143");
		
		when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> checkIAMCertificateExpired.execute(ruleParam, resourceAttribute)).isInstanceOf(InvalidInputException.class);
	}

	@Test
	public void getHelpTextTest() {
		assertThat(checkIAMCertificateExpired.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getValidIamCertificate(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("expirydate", "2032-08-19 10:22:25+0000");
		return resObj;
	}

	private Map<String, String> getExpiredIamCertificate(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("expirydate", "2022-05-13 10:22:25+0000");
		return resObj;
	}

}
