package com.tmobile.cloud.awsrules.iam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.tmobile.cloud.awsrules.utils.IAMUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BaseRule.class, IAMUtils.class })
public class AWSSupportAccessRoleTest {

	@InjectMocks
	AWSSupportAccessRole awsSupportAccessRole;

	@Mock
	AmazonIdentityManagementClient amazonIdentityManagementClient;

	@Before
	public void setUp() throws Exception {
		amazonIdentityManagementClient = PowerMockito.mock(AmazonIdentityManagementClient.class);
	}

	@Test
	public void iamAwsSupportRoleWithoutUser() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "3465697853542");
		
		Map<String, String> resourceAttribute = getResourceData("3465697853542");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		AWSSupportAccessRole spy = Mockito.spy(new AWSSupportAccessRole());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(IAMUtils.getAwsManagedPolicyArnByName(anyString(),anyObject())).thenReturn("arn:aws:iam::aws:policy/AWSSupportAccess");
		when(IAMUtils.getSupportRoleByPolicyArn(anyString(),anyObject())).thenReturn(mockRoleIds());
		when(PacmanUtils.getAssumedRolePolicies(anySetOf(String.class),anyString())).thenReturn(mockAssumedPolicies());
		when(IAMUtils.isSupportRoleAssumedByUserOrGroup(anySetOf(String.class), anyObject())).thenReturn(false);

		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	@Test
	public void iamAwsSupportRoleWithUser() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "3465697853542");
		
		Map<String, String> resourceAttribute = getResourceData("3465697853542");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		AWSSupportAccessRole spy = Mockito.spy(new AWSSupportAccessRole());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(IAMUtils.getAwsManagedPolicyArnByName(anyString(),anyObject())).thenReturn("arn:aws:iam::aws:policy/AWSSupportAccess");
		when(IAMUtils.getSupportRoleByPolicyArn(anyString(),anyObject())).thenReturn(mockRoleIds());
		when(PacmanUtils.getAssumedRolePolicies(anySetOf(String.class),anyString())).thenReturn(mockAssumedPolicies());
		when(IAMUtils.isSupportRoleAssumedByUserOrGroup(anySetOf(String.class), anyObject())).thenReturn(true);

		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "3465697853542");
		
		Map<String, String> resourceAttribute = getResourceData("3465697853542");

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> awsSupportAccessRole.execute(ruleParam, resourceAttribute)).isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void exceptionTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "3465697853542");
		
		Map<String, String> resourceAttribute = getResourceData("3465697853542");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		AWSSupportAccessRole spy = Mockito.spy(new AWSSupportAccessRole());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(IAMUtils.getAwsManagedPolicyArnByName(anyString(),anyObject())).thenReturn("arn:aws:iam::aws:policy/AWSSupportAccess");
		when(IAMUtils.getSupportRoleByPolicyArn(anyString(),anyObject())).thenThrow(new RuleExecutionFailedExeption());

		assertThatThrownBy(() -> spy.execute(ruleParam, resourceAttribute)).isInstanceOf(RuleExecutionFailedExeption.class);

	}
	
	@Test
	public void noAwsSupportRoleTest() throws Exception {

		mockStatic(PacmanUtils.class);
		mockStatic(IAMUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "3465697853542");
		
		Map<String, String> resourceAttribute = getResourceData("3465697853542");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		AWSSupportAccessRole spy = Mockito.spy(new AWSSupportAccessRole());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(IAMUtils.getAwsManagedPolicyArnByName(anyString(),anyObject())).thenReturn("arn:aws:iam::aws:policy/AWSSupportAccess");
		when(IAMUtils.getSupportRoleByPolicyArn(anyString(),anyObject())).thenReturn(null);

		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void getHelpTextTest() {
		assertThat(awsSupportAccessRole.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "AWS_Support_Access_Role_version-1_Support_Access_account");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_MEDIUM);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "3465697853542");
		ruleParam.put(PacmanSdkConstants.Role_IDENTIFYING_STRING, "test/ro");
		ruleParam.put("esIamRoleUrl", "esIamRoleUrl");
		return ruleParam;
	}

	private Map<String, String> getResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		resObj.put("accountid", id);
		return resObj;
	}
	
	private Set<String> mockRoleIds() {
		Set<String> roleIds = new HashSet<>();
		roleIds.add("test_role");
		roleIds.add("test_role1");
		roleIds.add("test_role1");
		return roleIds;
	}
	
	private Set<String> mockAssumedPolicies() {
		Set<String> policies = new HashSet<>();
		policies.add("policy1");
		policies.add("policy2");
		policies.add("policy3");
		return policies;
	}

}
