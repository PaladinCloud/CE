package com.tmobile.cloud.awsrules.federated;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
import com.tmobile.pacman.commons.rule.RuleResult;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class })
public class CheckACMCertificateExpiredTest {

	@InjectMocks
	CheckACMCertificateExpired checkACMCertificateExpired;

	@Test
	public void validACM() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "cloudsqlbackupruleid");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		Map<String, String> resourceAttribute = getValidACMCertificate("acm1234");
		RuleResult ruleResult = checkACMCertificateExpired.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void expiredACM() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "cloudsqlbackupruleid");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		Map<String, String> resourceAttribute = getExpiredACMCertificate("acm56789");
		RuleResult ruleResult = checkACMCertificateExpired.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void getHelpTextTest() {
		assertThat(checkACMCertificateExpired.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getValidACMCertificate(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("status", "ISSUED");
		return resObj;
	}

	private Map<String, String> getExpiredACMCertificate(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("status", "EXPIRED");
		return resObj;
	}

}
