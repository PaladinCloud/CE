package com.tmobile.cloud.awsrules.federated;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryResult;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BaseRule.class })
public class RemoveRootUserAccountAccessKeyTest {
	
	@InjectMocks
	RemoveRootUserAccountAccessKey removeRootUserAccountAccessKey;
	
	AmazonIdentityManagementClient amazonIdentityManagementClient;
	
	Map<String, String> ruleParam;
	
	RemoveRootUserAccountAccessKey spy;
	
	@Before
	public void setUp() throws Exception {
		amazonIdentityManagementClient = PowerMockito.mock(AmazonIdentityManagementClient.class);
		
		mockStatic(PacmanUtils.class);
		ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "test1");
		
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.checkNaclWithInvalidRules(anyString(), anyString(), anyString())).thenReturn(true);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		spy = Mockito.spy(new RemoveRootUserAccountAccessKey());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());

	}
	
	@Test
	public void executeTest() throws Exception {
		Map<String, String> resourceAttribute = getResourceData("test1");
		when(amazonIdentityManagementClient.getAccountSummary(anyObject())).thenReturn(mockSummary(0));
		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}
	
	@Test
	public void exectuteFails() throws Exception {
		Map<String, String> resourceAttribute = getResourceData("test1");
		when(amazonIdentityManagementClient.getAccountSummary(anyObject())).thenReturn(mockSummary(1));
		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void exectuteFailsWhenAccountAccessKeysPresentIsNull() throws Exception {
		Map<String, String> resourceAttribute = getResourceData("test1");
		when(amazonIdentityManagementClient.getAccountSummary(anyObject())).thenReturn(mockInvalidSummary());
		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}
	
	@Test
	public void mandatoryDataTest() throws Exception {
		Map<String, String> resourceAttribute = getResourceData("test1");
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> removeRootUserAccountAccessKey.execute(ruleParam, resourceAttribute))
				.isInstanceOf(InvalidInputException.class);

	}
	
	@Test
	public void getHelpTest() {
		assertNotNull(removeRootUserAccountAccessKey.getHelpText());
	}
	
	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "Account_version-1_Account");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		ruleParam.put(PacmanSdkConstants.Role_IDENTIFYING_STRING, "role");
		return ruleParam;
	}

	private Map<String, String> getResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		return resObj;
	}
	
	private GetAccountSummaryResult mockSummary(int accessKeyValue) {
		GetAccountSummaryResult result = new GetAccountSummaryResult();
		
		Map<String,Integer> map = new HashMap<>();
		map.put("AccountAccessKeysPresent", accessKeyValue);
		result.setSummaryMap(map);
		return result;
	}
	
	private GetAccountSummaryResult mockInvalidSummary() {
		GetAccountSummaryResult result = new GetAccountSummaryResult();
		
		Map<String,Integer> map = new HashMap<>();
		result.setSummaryMap(map);
		return result;
	}

}
