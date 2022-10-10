package com.tmobile.cloud.awsrules.federated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AssignmentStatusType;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryRequest;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryResult;
import com.amazonaws.services.identitymanagement.model.ListVirtualMFADevicesRequest;
import com.amazonaws.services.identitymanagement.model.ListVirtualMFADevicesResult;
import com.amazonaws.util.CollectionUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.AWSService;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.exception.UnableToCreateClientException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-hardware-MFA-root-account", desc = "Checks if root acount has hardware MFA configured", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class RootAccountHardwareMFACheck extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(RootAccountHardwareMFACheck.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : check-for-hardware-MFA-root-account <br><br>
	 *
	 * threadsafe : if true , rule will be executed on multiple threads <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 * 
	 * ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	@Override
	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========RootAccountHardwareMFACheck started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));
		
		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY),
						param.get(PacmanRuleConstants.CATEGORY), param.get(PacmanSdkConstants.Role_IDENTIFYING_STRING))))
				.map(param -> {
					logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
					throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
				});
		
		Optional<String> opt = Optional.ofNullable(resourceAttributes)
				.map(resource -> checkValidation(ruleParam, resource));
		
		RuleResult ruleResult = Optional.ofNullable(ruleParam)
				.filter(param -> opt.isPresent())
				.map(param -> buildFailureAnnotation(param, opt.get()))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));


		logger.debug("========RootAccountHardwareMFACheck ended=========");
		return ruleResult;

	}
    
	/**
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return
	 * 
	 * Validate the root account has enabled MFA and is hardware
	 * 
	 */
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		String description = null;
		Map<String, Object> map = null;
		AmazonIdentityManagementClient iamClient = null;
		Map<String, String> ruleParamforIAM = new HashMap<>();

		ruleParamforIAM.putAll(ruleParam);
		ruleParamforIAM.put("region", "us-east-1");

		try {
			map = getClientFor(AWSService.IAM, ruleParamforIAM.get(PacmanSdkConstants.Role_IDENTIFYING_STRING), ruleParamforIAM);
			iamClient = (AmazonIdentityManagementClient) map.get(PacmanSdkConstants.CLIENT);

			GetAccountSummaryRequest request = new GetAccountSummaryRequest();
			GetAccountSummaryResult response = iamClient.getAccountSummary(request);
			Map<String, Integer> summaryMap = response.getSummaryMap();

			if (summaryMap.get("AccountMFAEnabled") == 1) {
				ListVirtualMFADevicesRequest listMfaRequest = new ListVirtualMFADevicesRequest();
				ListVirtualMFADevicesResult result = new ListVirtualMFADevicesResult();
				result = iamClient
						.listVirtualMFADevices(listMfaRequest.withAssignmentStatus(AssignmentStatusType.Assigned));
				if (null != result && !CollectionUtils.isNullOrEmpty(result.getVirtualMFADevices())) {
					if (result.getVirtualMFADevices().stream()
							.filter(device -> device.getSerialNumber().contains("root-account-mfa-device")).count() > 0)
						return description = "Hardware MFA device is not configured for root account !!";
				}

			} else {
				return description = "Hardware MFA device is not configured for root account !!";
			}

		} catch (UnableToCreateClientException e) {
			logger.error("unable to get client for following input", e);
			throw new InvalidInputException(e.toString());
		} catch (Exception e) {
			logger.error("unable to determine", e);
			throw new RuleExecutionFailedExeption("unable to determine" + e);
		}
		return description;
	}

	private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {
		
		Annotation annotation = null;
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		annotation = Annotation.buildAnnotation(ruleParam,Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION,description);
		annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
		annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========RootAccountHardwareMFACheck annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}
	
	@Override
	public String getHelpText() {
		return "Checks if root acount has hardware MFA configured";
	}

}
