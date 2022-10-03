package com.tmobile.cloud.awsrules.federated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryRequest;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryResult;
import com.amazonaws.util.StringUtils;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.AWSService;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.UnableToCreateClientException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "remove-root-user-account-access-key", desc = "Checks if the the root user account access key is removed.", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class RemoveRootUserAccountAccessKey extends BaseRule{
	
	private static final Logger logger = LoggerFactory.getLogger(RemoveRootUserAccountAccessKey.class);

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 *
	 * ruleKey : remove-root-user-account-access-key <br><br>
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
	public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		logger.debug("========CheckForRootUserAccountAccessKey started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));
		String roleIdentifyingString = ruleParam.get(PacmanSdkConstants.Role_IDENTIFYING_STRING);

		if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
				ruleParam.get(PacmanRuleConstants.CATEGORY), roleIdentifyingString)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		GetAccountSummaryResult response = getAccountSummaryResponse(ruleParam);

		if(Objects.isNull(response) || Objects.isNull(response.getSummaryMap())) {
			logger.error(PacmanRuleConstants.INVALID_ACCOUNT_SUMMARY_RESPONSE);
			throw new InvalidInputException(PacmanRuleConstants.INVALID_ACCOUNT_SUMMARY_RESPONSE);
		}
		
		Optional<String> opt = Optional.ofNullable(response.getSummaryMap())
				.map(resource -> checkValidation(resource));
		RuleResult ruleResult = Optional.ofNullable(ruleParam).filter(param -> opt.isPresent())
				.map(param -> buildFailureAnnotation(param, opt.get()))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));

		logger.debug("========CheckForRootUserAccountAccessKey ended=========");
		return ruleResult;
	}
	
	private GetAccountSummaryResult getAccountSummaryResponse(Map<String, String> ruleParam) {
		Map<String, String> temp = new HashMap<>();
		temp.putAll(ruleParam);
		temp.put("region", "us-west-2");
		GetAccountSummaryResult response = null;
		try {
			Map<String, Object> map = getClientFor(AWSService.IAM, ruleParam.get(PacmanSdkConstants.Role_IDENTIFYING_STRING), temp);
			AmazonIdentityManagementClient identityManagementClient = (AmazonIdentityManagementClient) map.get(PacmanSdkConstants.CLIENT);
			response = identityManagementClient.getAccountSummary(new GetAccountSummaryRequest());
			
			} catch (UnableToCreateClientException utcce) {
				logger.error(PacmanRuleConstants.UNABLE_TO_GET_CLIENT_FOR_FOLLOWING_INPUT, utcce);
				throw new InvalidInputException(utcce.toString());
			} catch (Exception e) {
				logger.error(PacmanRuleConstants.UNABLE_TO_GET_CLIENT_FOR_FOLLOWING_INPUT, e);
				throw new InvalidInputException(e.toString());
			}
		return response;
	}
	
	/**
	 * Checks if the the root user account access key is removed
	 * 
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return string
	 * 
	 */
	private String checkValidation(Map<String, Integer> summaryMap) {

		String description = null;
		Optional<Integer> value = Optional.ofNullable(summaryMap.get(PacmanRuleConstants.ACCOUNT_ACCESS_KEYS_PRESENT));

		if (value.isPresent() && !Objects.isNull(value.get()) && value.get() == 1) {
			description = "Root user account access key is present";
		}
		return description;
	}
	
	private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
		Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION, description);
		annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
		annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
		issueList.add(issue);
		annotation.put("issueDetails", issueList.toString());
		logger.debug("========RemoveRootUserAccountAccessKey annotation {} :=========", annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	}

	@Override
	public String getHelpText() {
		return "Checks if the the root user account access key is removed.";
	}

}
