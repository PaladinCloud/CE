package com.tmobile.cloud.awsrules.federated;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import com.amazonaws.services.identitymanagement.model.AssignmentStatusType;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryResult;
import com.amazonaws.services.identitymanagement.model.ListVirtualMFADevicesRequest;
import com.amazonaws.services.identitymanagement.model.ListVirtualMFADevicesResult;
import com.amazonaws.services.identitymanagement.model.VirtualMFADevice;
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
public class RootAccountHardwareMFACheckTest {

	@InjectMocks
	RootAccountHardwareMFACheck rootAccountHardwareMFACheck;

	@Mock
	AmazonIdentityManagementClient amazonIdentityManagementClient;

	@Before
	public void setUp() throws Exception {
		amazonIdentityManagementClient = PowerMockito.mock(AmazonIdentityManagementClient.class);
	}

	@Test
	public void invalidMFA() throws Exception {

		mockStatic(PacmanUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "123456789");
		
		Map<String, String> resourceAttribute = getResourceData("123456789");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		RootAccountHardwareMFACheck spy = Mockito.spy(new RootAccountHardwareMFACheck());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(amazonIdentityManagementClient.getAccountSummary(anyObject())).thenReturn(mockSummary());
		
		ListVirtualMFADevicesRequest listMfaRequest = new ListVirtualMFADevicesRequest();
		when(amazonIdentityManagementClient.listVirtualMFADevices(listMfaRequest.withAssignmentStatus(AssignmentStatusType.Assigned))).thenReturn(mockMFAList());
		
		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}

	

	@Test
	public void validMFA() throws Exception {

		mockStatic(PacmanUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "123456789");
		
		Map<String, String> resourceAttribute = getResourceData("123456789");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		RootAccountHardwareMFACheck spy = Mockito.spy(new RootAccountHardwareMFACheck());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(amazonIdentityManagementClient.getAccountSummary(anyObject())).thenReturn(mockSummary());
		
		ListVirtualMFADevicesRequest listMfaRequest = new ListVirtualMFADevicesRequest();
		when(amazonIdentityManagementClient.listVirtualMFADevices(listMfaRequest.withAssignmentStatus(AssignmentStatusType.Assigned))).thenReturn(new ListVirtualMFADevicesResult());
		
		RuleResult ruleResult = spy.execute(ruleParam, resourceAttribute);

		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}

	@Test
	public void mandatoryDataTest() throws Exception {

		mockStatic(PacmanUtils.class);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "123456789");

		Map<String, String> resourceAttribute = getResourceData("123456789");

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> rootAccountHardwareMFACheck.execute(ruleParam, resourceAttribute)).isInstanceOf(InvalidInputException.class);

	}

	@Test
	public void exceptionTest() throws Exception {

		mockStatic(PacmanUtils.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);

		Map<String, String> ruleParam = getInputParamMap();
		ruleParam.put(PacmanRuleConstants.RESOURCE_ID, "123456789");
		
		Map<String, String> resourceAttribute = getResourceData("123456789");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("client", amazonIdentityManagementClient);
		RootAccountHardwareMFACheck spy = Mockito.spy(new RootAccountHardwareMFACheck());
		Mockito.doReturn(map).when((BaseRule) spy).getClientFor(anyObject(), anyString(), anyObject());
		
		when(amazonIdentityManagementClient.getAccountSummary(anyObject())).thenThrow(new RuleExecutionFailedExeption());
		assertThatThrownBy(() -> spy.execute(ruleParam, resourceAttribute)).isInstanceOf(RuleExecutionFailedExeption.class);

	}
	
	@Test
	public void getHelpTextTest() {
		assertThat(rootAccountHardwareMFACheck.getHelpText(), is(notNullValue()));
	}

	private Map<String, String> getInputParamMap() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "AWS_rootaccount_hardware_MFA_version-1_enable_harware_mfa_account");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_MEDIUM);
		ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
		ruleParam.put(PacmanSdkConstants.Role_IDENTIFYING_STRING, "test/ro");
		return ruleParam;
	}

	private Map<String, String> getResourceData(String id) {
		Map<String, String> resObj = new HashMap<>();
		resObj.put("_resourceid", id);
		return resObj;
	}
	
	private ListVirtualMFADevicesResult mockMFAList() {
		
		ListVirtualMFADevicesResult result = new ListVirtualMFADevicesResult();
		VirtualMFADevice device1 = new VirtualMFADevice();
		device1.setSerialNumber("122131312");
		VirtualMFADevice device2 = new VirtualMFADevice();
		device2.setSerialNumber("5654767676534");
		result.setVirtualMFADevices(Arrays.asList(device1,device2));
		return result;
	}
	
	private GetAccountSummaryResult mockSummary() {
		GetAccountSummaryResult result = new GetAccountSummaryResult();
		
		Map<String,Integer> map = new HashMap<>();
		map.put("AccountMFAEnabled", 1);
		result.setSummaryMap(map);
		return result;
	}


}
