package com.tmobile.cloud.awsrules.iam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.IAMUtils;
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

@PacmanRule(key = "iam-role-for-aws-support-access", desc = "Checks any dedicated IAM role has created for AWS support access", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class AWSSupportAccessRole extends BaseRule {

	/** The Constant LOGGER. */

	private static final Logger logger = LoggerFactory.getLogger(AWSSupportAccessRole.class);
	private static final String POLICY_NAME = "policyName";

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : iam-role-for-aws-support-access <br><br>
	 *
	 * threadsafe : if true , rule will be executed on multiple threads <br><br>
	 * 
	 * policyArn : Enter the value of policy arn <br><br>
	 * 
	 * roleIdentifyingString : Enter the value of role to fetch iam <br><br>
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

		logger.debug("========AWSSupportAccessRole started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));
		
		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY),
						param.get(PacmanRuleConstants.CATEGORY), param.get(PacmanSdkConstants.Role_IDENTIFYING_STRING),
						param.get(POLICY_NAME))))
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


		logger.debug("========AWSSupportAccessRole ended=========");
		return ruleResult;

	}
    
	/**
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return
	 * 
	 * Validate any separate IAM role created for AWSSupportAccess and attached to user or group
	 * Methods used
	 * <p>
	 * <p>
	 * getAwsManagedPolicyArnByName(String,AmazonIdentityManagementClient) : Fetch the arn of AWS managed AWSSupportAccess policy
	 * getSupportRoleByPolicyArn(String,AmazonIdentityManagementClient) : Fetch all the role ids having the AWSSupportAccess policy 
	 * attached
	 * getAssumedRolePolicies(Set<String>,String) : Fetch the trusted policies of the support roles
	 * isSupportRoleAssumedByUserOrGroup(Set<String>,AmazonIdentityManagementClient) : Check the trusted policy having any user or 
	 * group. If no, then the rule fails. Rule fails if no role is attached with the AWSSupportAccess policy
	 * 
	 */
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		Map<String, Object> map = null;
		AmazonIdentityManagementClient iamClient = null;
		Map<String, String> ruleParamforIAM = new HashMap<>();
		String description = "Dedicated IAM role for AWS Support Access should be created and attached to user or group!!";

		String policyName = ruleParam.get(POLICY_NAME);

		ruleParamforIAM.putAll(ruleParam);
		ruleParamforIAM.put("region", "us-east-1");
		
		String formattedRoleUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_IAM_ROLE_URL);
		String esIamRoleUrl = !StringUtils.isNullOrEmpty(formattedRoleUrl) ? formattedRoleUrl : "";

		try {
			map = getClientFor(AWSService.IAM, ruleParamforIAM.get(PacmanSdkConstants.Role_IDENTIFYING_STRING), ruleParamforIAM);
			iamClient = (AmazonIdentityManagementClient) map.get(PacmanSdkConstants.CLIENT);
			
			String policyArn = IAMUtils.getAwsManagedPolicyArnByName(policyName, iamClient);
			
			if(!StringUtils.isNullOrEmpty(policyArn)) {
				
				Set<String> supportRoles = IAMUtils.getSupportRoleByPolicyArn(policyArn, iamClient);
				if (!CollectionUtils.isNullOrEmpty(supportRoles)) {
					Set<String> policies = PacmanUtils.getAssumedRolePolicies(supportRoles,esIamRoleUrl);
					if (!CollectionUtils.isNullOrEmpty(policies)) {
						boolean isAssumedRole = IAMUtils.isSupportRoleAssumedByUserOrGroup(policies, iamClient);
						if(!isAssumedRole)
							return description;
					}
				}else
					return description;
			}
			
		} catch (UnableToCreateClientException e) {
			logger.error("unable to get client for following input", e);
			throw new InvalidInputException(e.toString());
		} catch (Exception e) {
			logger.error("unable to determine", e);
			throw new RuleExecutionFailedExeption("unable to determine" + e);
		}
		return null;
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
		logger.debug("========AWSSupportAccessRole annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}
	
	@Override
	public String getHelpText() {
		return "Checks any dedicated IAM role has created for AWS support access";
	}

}
