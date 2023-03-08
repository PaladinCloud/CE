package com.tmobile.pacman.api.admin.controller;

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.CreateUpdatePolicyDetails;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.model.Policy;
import com.tmobile.pacman.api.admin.repository.service.PolicyService;

@RunWith(MockitoJUnitRunner.class)
public class PolicyControllerTest {
	private MockMvc mockMvc;

	private Principal principal;

	@Mock
	private PolicyService policyService;

	@InjectMocks
	private PolicyController policyController;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(policyController)
				/* .addFilters(new CORSFilter()) */
				.build();
		principal = Mockito.mock(Principal.class);
	}

	@Test
	public void getPoliciesTest() throws Exception {
		List<Policy> policyDetails = new ArrayList<Policy>();
		Policy policyDetail = getPolicyDetailsResponse();
		policyDetails.add(policyDetail);
		Page<Policy> allPolicies = new PageImpl<Policy>(policyDetails, new PageRequest(0, 1), policyDetails.size());

		when(policyService.getPolicies(anyString(), anyInt(), anyInt())).thenReturn(allPolicies);
		mockMvc.perform(get("/policy/list").param("searchTerm", StringUtils.EMPTY).param("page", "0").param("size", "1"))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.message", is("success")));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getPoliciesExceptionTest() throws Exception {
		when(policyService.getPolicies(anyString(), anyInt(), anyInt())).thenThrow(Exception.class);
		mockMvc.perform(get("/policy/list").param("searchTerm", StringUtils.EMPTY).param("page", "0").param("size", "1"))
				.andExpect(status().isExpectationFailed()).andExpect(jsonPath("$.message", is(UNEXPECTED_ERROR_OCCURRED)));
	}

	@Test
	public void getPoliciesByIdTest() throws Exception {
		when(policyService.getByPolicyId(eq(StringUtils.EMPTY))).thenReturn(getPolicyDetailsResponse());
		mockMvc.perform(get("/policy/details-by-id")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.message", is("success"))).andExpect(jsonPath("$.data.ruleId", is("ruleId123")));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getPoliciesByIdExceptionTest() throws Exception {
		when(policyService.getByPolicyId(eq(StringUtils.EMPTY))).thenThrow(Exception.class);
		mockMvc.perform(get("/policy/details-by-id").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isExpectationFailed()).andExpect(jsonPath("$.message", is(UNEXPECTED_ERROR_OCCURRED)));
	}

	@Test
	public void getAllAlexaKeywordsTest() throws Exception {
		when(policyService.getAllAlexaKeywords()).thenReturn(getAllAlexaKeywordsResponse());
		mockMvc.perform(get("/policy/alexa-keywords")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.message", is("success"))).andExpect(jsonPath("$.data[0]", is("alexaKeywords1")))
				.andExpect(jsonPath("$.data", hasSize(3)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getAllAlexaKeywordsExceptionTest() throws Exception {
		when(policyService.getAllAlexaKeywords()).thenThrow(Exception.class);
		mockMvc.perform(get("/policy/alexa-keywords").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isExpectationFailed()).andExpect(jsonPath("$.message", is(UNEXPECTED_ERROR_OCCURRED)));
	}

	@Test
	public void invokePolicyTest() throws Exception {
		String invocationId = "123";
		byte[] ruleOptionalParamsContent = toJson(Lists.newArrayList());
		when(policyService.invokePolicy(any(), any())).thenReturn(invocationId);
		mockMvc.perform(post("/policy/invoke").param("policyId", "policyId123").content(ruleOptionalParamsContent)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("success"))).andExpect(jsonPath("$.data", is("123")));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void invokePolicyExceptionTest() throws Exception {
		byte[] ruleOptionalParamsContent = toJson(Lists.newArrayList());
		when(policyService.invokePolicy(anyString(), anyList())).thenThrow(Exception.class);
		mockMvc.perform(post("/policy/invoke").param("policyId", "policyId123").content(ruleOptionalParamsContent)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isExpectationFailed()).andExpect(jsonPath("$.message", is(UNEXPECTED_ERROR_OCCURRED)));
	}

	@Test
	public void enableDisablePolicyTest() throws Exception {
		when(policyService.enableDisablePolicy(anyString(), anyString(), anyString()))
				.thenReturn(AdminConstants.POLICY_DISABLE_ENABLE_SUCCESS);
		mockMvc.perform(post("/policy/enable-disable").principal(principal).param("policyId", "policyId123")
				.param("action", "action123").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("success"))).andDo(print())
				.andExpect(jsonPath("$.data", is(AdminConstants.POLICY_DISABLE_ENABLE_SUCCESS)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void enableDisablePolicyExceptionTest() throws Exception {
		when(policyService.enableDisablePolicy(any(), any(), any())).thenThrow(Exception.class);
		mockMvc.perform(post("/policy/enable-disable").principal(principal).param("policyId", "policyId123")
				.param("action", "action123").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isExpectationFailed()).andDo(print())
				.andExpect(jsonPath("$.message", is(UNEXPECTED_ERROR_OCCURRED)));
	}

	@Test
	public void createPolicyTest() throws Exception {
		byte[] policyDetailsContent = toJson(getCreateUpdateRuleDetailsRequest());
		MultipartFile firstFile = getMockMultipartFile();
		when(policyService.createPolicy(any(), any(), any())).thenReturn(AdminConstants.POLICY_CREATION_SUCCESS);
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/policy/create").file("file", firstFile.getBytes())
				.principal(principal).content(policyDetailsContent).contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
				.andExpect(status().isOk()).andExpect(jsonPath("$.message", is("success")))
				.andExpect(jsonPath("$.data", is(AdminConstants.POLICY_CREATION_SUCCESS)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void createPolicyExceptionTest() throws Exception {
		byte[] policyDetailsContent = toJson(getCreateUpdateRuleDetailsRequest());
		MultipartFile firstFile = getMockMultipartFile();
		when(policyService.createPolicy(any(), any(), any())).thenThrow(Exception.class);
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/policy/create").file("file", firstFile.getBytes())
				.principal(principal).content(policyDetailsContent).contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
				.andExpect(status().isExpectationFailed()).andExpect(jsonPath("$.message", is(UNEXPECTED_ERROR_OCCURRED)));
	}

	@Test
	public void updatePolicyTest() throws Exception {
		byte[] policyDetailsContent = toJson(getCreateUpdateRuleDetailsRequest());
		MultipartFile firstFile = getMockMultipartFile();
		when(policyService.updatePolicy(any(), any(), any())).thenReturn(AdminConstants.POLICY_CREATION_SUCCESS);
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/policy/update").file("file", firstFile.getBytes())
				.principal(principal).content(policyDetailsContent).contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
				.andExpect(status().isOk()).andExpect(jsonPath("$.message", is("success")))
				.andExpect(jsonPath("$.data", is(AdminConstants.POLICY_CREATION_SUCCESS)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void updatePolicyExceptionTest() throws Exception {
		byte[] policyDetailsContent = toJson(getCreateUpdateRuleDetailsRequest());
		MultipartFile firstFile = getMockMultipartFile();
		when(policyService.updatePolicy(any(), any(), any())).thenThrow(Exception.class);
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/policy/update").file("file", firstFile.getBytes())
				.principal(principal).content(policyDetailsContent).contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
				.andExpect(status().isExpectationFailed()).andExpect(jsonPath("$.message", is(UNEXPECTED_ERROR_OCCURRED)));
	}
	
	@Test
	public void getAllPolicyCategoryTest() throws PacManException {
		
		when(policyService.getAllPolicyCategories()).thenReturn(new ArrayList<>());
        assertThat(policyController.getAllPolicyCategory(), is(notNullValue()));
        
        when(policyService.getAllPolicyCategories()).thenThrow(new PacManException("error"));
        assertTrue(policyController.getAllPolicyCategory().getStatusCode() == HttpStatus.EXPECTATION_FAILED);
	}

	private MultipartFile getMockMultipartFile() {
		return new MockMultipartFile("data", "rule.jar", "multipart/form-data", "rule content".getBytes());
	}

	private CreateUpdatePolicyDetails getCreateUpdateRuleDetailsRequest() {
		CreateUpdatePolicyDetails policyDetails = new CreateUpdatePolicyDetails();
		policyDetails.setPolicyId("policyId123");
		policyDetails.setPolicyName("policyName123");
		policyDetails.setTargetType("targetType123");
		policyDetails.setAssetGroup("assetGroup123");
		policyDetails.setAlexaKeyword("alexaKeyword123");
		policyDetails.setPolicyParams("policyParams123");
		policyDetails.setPolicyFrequency("policyFrequency123");
		policyDetails.setPolicyExecutable("policyExecutable123");
		policyDetails.setPolicyRestUrl("policyRestUrl123");
		policyDetails.setPolicyType("policyType123");
		policyDetails.setStatus("status123");
		policyDetails.setPolicyDisplayName("displayName123");
		policyDetails.setDataSource("dataSource123");
		policyDetails.setAutofixEnabled("false");
		policyDetails.setIsFileChanged(false);
		return policyDetails;
	}

	private Collection<String> getAllAlexaKeywordsResponse() {
		Collection<String> allAlexaKeywords = new ArrayList<String>();
		allAlexaKeywords.add("alexaKeywords1");
		allAlexaKeywords.add("alexaKeywords2");
		allAlexaKeywords.add("alexaKeywords3");
		return allAlexaKeywords;
	}

	private Policy getPolicyDetailsResponse() {
		Policy policy = new Policy();
		policy.setPolicyUUID("UUID123");
		policy.setPolicyId("policyId123");
		policy.setPolicyName("policyName123");
		policy.setTargetType("targetType123");
		policy.setAssetGroup("assetGroup123");
		policy.setAlexaKeyword("alexaKeyword123");
		policy.setPolicyParams("{\"assetGroup\":\"aws\",\"environmentVariables\":[{\"encrypt\":false,\"value\":\"123\",\"key\":\"abc\"}],\"policyUUID\":\"22ce851c-7b6c-4986-9ba9-db97803b363a\",\"policyType\":\"Managepolicy\",\"pac_ds\":\"aws\",\"targetType\":\"cloudtrl\",\"params\":[{\"encrypt\":\"false\",\"value\":\"role/pac_ro\",\"key\":\"roleIdentifyingString\"},{\"encrypt\":\"false\",\"value\":\"check-for-aws-cloudtrail-config\",\"key\":\"policyKey\"},{\"encrypt\":false,\"value\":\"critical\",\"key\":\"severity\"},{\"encrypt\":false,\"value\":\"security\",\"key\":\"policyCategory\"}],\"policyId\":\"PacMan_AWSCloudTrailConfig_version-1_AWSCloudTrailConfig_cloudtrl\",\"autofix\":false,\"alexaKeyword\":\"AWSCloudTrailConfig\",\"policyRestUrl\":\"\"}");
		policy.setPolicyFrequency("policyFrequency123");
		policy.setPolicyExecutable("policyExecutable123");
		policy.setPolicyRestUrl("policyRestUrl123");
		policy.setPolicyType("policyType123");
		policy.setPolicyArn("policyArn123");
		policy.setStatus("status123");
		policy.setUserId("userId123");
		policy.setPolicyDesc("policy descritpion");
		policy.setResolution("resolution");
		policy.setResolutionUrl("resolution url");
		policy.setPolicyDisplayName("displayName123");
		policy.setCreatedDate(new Date());
		policy.setModifiedDate(new Date());
		return policy;
	}

	private byte[] toJson(Object r) throws Exception {
		ObjectMapper map = new ObjectMapper();
		return map.writeValueAsString(r).getBytes();
	}
}
