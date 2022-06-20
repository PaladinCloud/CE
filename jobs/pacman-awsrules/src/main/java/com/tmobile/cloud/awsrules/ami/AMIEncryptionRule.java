package com.tmobile.cloud.awsrules.ami;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-for-ami-blockdevice-is-encrypted", desc = "checks for AWS AMI block devices attached are encrypted", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class AMIEncryptionRule extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(AMIEncryptionRule.class);
	
	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 ************** Following are the Rule Parameters********* <br><br>
	 * 
	 * ruleKey : check-for-ami-blockdevice-is-encrypted <br><br>
	 *
	 * threadsafe : if true , rule will be executed on multiple threads <br><br>
	 * 
	 * esAmiBlockDeviceMappingUrl : Enter the ami block device es api url <br><br>
	 *
	 * severity : Enter the value of severity <br><br>
	 * 
	 * ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */

	public RuleResult execute(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		logger.debug("========AMIEncryptionRule started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		Optional.ofNullable(ruleParam)
				.filter(param -> (!PacmanUtils.doesAllHaveValue(param.get(PacmanRuleConstants.SEVERITY),
						param.get(PacmanRuleConstants.CATEGORY),
						param.get(PacmanRuleConstants.ES_AMI_BLOCK_DEVICE_MAPPING_URL))))
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


		logger.debug("========AMIEncryptionRule ended=========");
		return ruleResult;

	}
	
	private String checkValidation(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		
		String description = null;
		String formattedAmiBlockDeviceUrl = PacmanUtils.formatUrl(ruleParam, PacmanRuleConstants.ES_AMI_BLOCK_DEVICE_MAPPING_URL);
		String esAmiBlockDeviceMappingUrl = !StringUtils.isNullOrEmpty(formattedAmiBlockDeviceUrl) ? formattedAmiBlockDeviceUrl : "";
		try {
			List<String> snapshotIds = PacmanUtils.getUnencryptedSnapshotIds(resourceAttributes.get(PacmanRuleConstants.ES_IMAGE_ID_ATTRIBUTE), esAmiBlockDeviceMappingUrl);
			if (!CollectionUtils.isNullOrEmpty(snapshotIds))
				description = "Not encrypted AWS AMI block device snapshot(s) found -  " + String.join(",", snapshotIds);
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
		issue.put(PacmanRuleConstants.VIOLATION_REASON, "Encryption is not enabled for the AWS AMI.");
		issueList.add(issue);
		annotation.put("issueDetails",issueList.toString());
		logger.debug("========AMIEncryptionRule annotation {} :=========",annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE,PacmanRuleConstants.FAILURE_MESSAGE, annotation);
	
	
	}

	public String getHelpText() {
		return "This rule checks for AWS AMI block devices attached are encrypted";
	}
	
}
