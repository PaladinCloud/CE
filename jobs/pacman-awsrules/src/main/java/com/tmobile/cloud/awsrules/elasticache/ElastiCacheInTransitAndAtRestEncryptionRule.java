package com.tmobile.cloud.awsrules.elasticache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PacmanPolicy(key = "check-for-elasticache-clusters-encrypted", desc = "checks for Amazon ElastiCache Redis clusters enabled in-transit and at-rest encryption", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class ElastiCacheInTransitAndAtRestEncryptionRule extends BasePolicy {
	private static final Logger logger = LoggerFactory.getLogger(ElastiCacheInTransitAndAtRestEncryptionRule.class);
	public static final String CLUSTER_ENGINE_TYPE = "engineType";
	public static final String CLUSTER_ENGINE_VERSION = "engineVersion";

	public static final String ES_PROP_ENGINE = "engine";
	public static final String ES_PROP_ENGINEVERSION = "engineversion";
	public static final String ES_PROP_AT_REST_ENCRY = "atrestencryptionenabled";
	public static final String ES_PROP_AT_TRANSIT_ENCRY = "transitencryptionenabled";

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 **************Following are the Rule Parameters********* <br>
	 *                           <br>
	 * 
	 *                           ruleKey : check-for-elasticache-clusters-encrypted
	 *                           <br>
	 *                           <br>
	 * 
	 *                           threadsafe : if true , rule will be executed on
	 *                           multiple threads <br>
	 *                           <br>
	 * 
	 *                           severity : Enter the value of severity <br>
	 *                           <br>
	 * 
	 *                           ruleCategory : Enter the value of category <br>
	 *                           <br>
	 *
	 *                           engineType : Enter the Cluster Engine type to
	 *                           validate <br>
	 *                           <br>
	 *
	 *                           engineVersion : Enter the Cluster Engine version to
	 *                           validate <br>
	 *                           <br>
	 *
	 * @param resourceAttributes this is a resource in context which needs to be
	 *                           scanned this is provided by execution engine
	 *
	 */
	@Override
	public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
		logger.debug("========ElastiCacheEncryptionRule started=========");
		Annotation annotation = null;

		String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
		String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
		String clusterEngineType = ruleParam.get(CLUSTER_ENGINE_TYPE);
		String clusterEngineVersion = ruleParam.get(CLUSTER_ENGINE_VERSION);

		MDC.put("executionId", ruleParam.get("executionId")); // this is the logback Mapped Diagnostic Contex
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID)); // this is the logback Mapped Diagnostic Contex

		if (!PacmanUtils.doesAllHaveValue(severity, category, clusterEngineType, clusterEngineVersion)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}
		if (resourceAttributes != null) {

			String engine = StringUtils.trim(resourceAttributes.get(ES_PROP_ENGINE));
			String engVersion = StringUtils.trim(resourceAttributes.get(ES_PROP_ENGINEVERSION));
			String restEncry = StringUtils.trim(resourceAttributes.get(ES_PROP_AT_REST_ENCRY));
			String transitEncry = StringUtils.trim(resourceAttributes.get(ES_PROP_AT_TRANSIT_ENCRY));
			if (engine != null && engVersion != null && clusterEngineType.equalsIgnoreCase(engine)
					&& clusterEngineVersion.equalsIgnoreCase(engVersion)) {
				if (transitEncry == null || restEncry == null
						|| !PacmanRuleConstants.TRUE_VAL.equalsIgnoreCase(transitEncry)
						|| !PacmanRuleConstants.TRUE_VAL.equalsIgnoreCase(restEncry)) {
					List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
					LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
					annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
					annotation.put(PacmanSdkConstants.DESCRIPTION, "Unused encrypted Elasticache found!!");
					annotation.put(PacmanRuleConstants.SEVERITY, severity);;
					annotation.put(PacmanRuleConstants.CATEGORY, category);
					issue.put(PacmanRuleConstants.VIOLATION_REASON,
							"Elasticache in-transit and at-rest are not encrypted!!");
					issueList.add(issue);
					annotation.put("issueDetails", issueList.toString());
					logger.debug("========ElastiCacheEncryptionRule ended with annotation {} :=========", annotation);
					return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
							annotation);
				}
			}
		}
		logger.debug("========ElastiCacheEncryptionRule ended=========");
		return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

	}

	@Override
	public String getHelpText() {
		return "This rule checks for Amazon ElastiCache Redis clusters enabled in-transit and at-rest encryption";
	}

}
