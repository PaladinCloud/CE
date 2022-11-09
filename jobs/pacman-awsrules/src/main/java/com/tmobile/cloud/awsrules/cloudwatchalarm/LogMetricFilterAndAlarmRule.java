package com.tmobile.cloud.awsrules.cloudwatchalarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;

@PacmanRule(key = "check-cloudwatch-alarm-exists", desc = "This rule checks for log metric filter and alarm exists", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class LogMetricFilterAndAlarmRule extends BaseRule {

	private static final Logger logger = LoggerFactory.getLogger(LogMetricFilterAndAlarmRule.class);

    private static final String CLOUD_TRAIL_URL = "/aws/cloudtrail/_search";
    private static final String CLOUD_WATCH_LOGS_METRIC_URL = "/aws/cloudwatchlogs_metric/_search";
    private static final String CLOUD_WATCH_ALARM_URL = "/aws/cloudwatchalarm/_search";

	/**
	 * The method will get triggered from Rule Engine with following parameters
	 * 
	 * @param ruleParam
	 * 
	 **************Following are the Rule Parameters********* <br><br>
	 *
	 *ruleKey :check-cloudwatch-alarm-exists <br><br>
	 *
	 *threadsafe : if true , rule will be executed on multiple threads <br><br>
	 *
	 *severity : Enter the value of severity <br><br>
	 * 
	 *ruleCategory : Enter the value of category <br><br>
	 * 
	 * @param resourceAttributes this is a resource in context which needs to be scanned this is provided by execution engine
	 *
	 */
	@Override
	public RuleResult execute(final Map<String, String> ruleParam, final Map<String, String> resourceAttributes) {

		logger.debug("========CheckForLogMetricFilterAndAlarmEnabled started=========");

		MDC.put("executionId", ruleParam.get("executionId"));
		MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

		if (MapUtils.isNotEmpty(ruleParam) && !PacmanUtils.doesAllHaveValue(ruleParam.get(PacmanRuleConstants.SEVERITY),
				ruleParam.get(PacmanRuleConstants.CATEGORY))) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		Optional<String> opt = Optional.ofNullable(resourceAttributes)
				.map(resource -> checkValidation(ruleParam, resource));

		RuleResult ruleResult = Optional.ofNullable(ruleParam).filter(param -> opt.isPresent())
				.map(param -> buildFailureAnnotation(param, opt.get()))
				.orElse(new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE));

		logger.debug("========CheckForLogMetricFilterAndAlarmEnabled ended=========");
		return ruleResult;

	}

	/**
	 * 
	 * Checks log metric filter and alarm exists.
	 * 
	 * @param ruleParam
	 * @param resourceAttributes
	 * @return string
	 * 
	 */
	private String checkValidation(final Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

		String description = null;

		String esCloudTrailPubAccessUrl = null;
		String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);

		String metricName = ruleParam.get(PacmanRuleConstants.METRIC_NAME);
		String metricNamespace = ruleParam.get(PacmanRuleConstants.METRIC_NAMESPACE);
		String filterName = ruleParam.get(PacmanRuleConstants.FILTER_NAME);
		String accountId = resourceAttributes.get(PacmanRuleConstants.ACCOUNTID);
		logger.debug("========pacmanHost {}  =========", pacmanHost);

		if (!StringUtils.isNullOrEmpty(pacmanHost)) {
			esCloudTrailPubAccessUrl = CLOUD_TRAIL_URL;
			esCloudTrailPubAccessUrl = pacmanHost + esCloudTrailPubAccessUrl;
		}

		if (!PacmanUtils.doesAllHaveValue(esCloudTrailPubAccessUrl, accountId, metricName, metricNamespace,
				filterName)) {
			logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
			throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
		}

		try {

			Map<String, Object> mustFilter = new HashMap<>();
			mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
			mustFilter.put(PacmanRuleConstants.MULTI_REGION_TRAIL, "true");
			mustFilter.put(PacmanRuleConstants.LOGGING, "true");
			HashMultimap<String, Object> shouldFilter = HashMultimap.create();
			Map<String, Object> mustTermsFilter = new HashMap<>();

			Set<String> resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter,
					shouldFilter, mustTermsFilter, "_resourceid", null);

			if (Objects.isNull(resultSet) || resultSet.isEmpty()) {
				return "CloudTrail log with matching conditions does not exists,isMultiRegionTrail: true"
						+ ",isLogging: true,accountId: " + accountId;
			}

			mustFilter = new HashMap<>();
			mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
			mustFilter.put(PacmanRuleConstants.METRIC_NAME, metricName);
			mustFilter.put(PacmanRuleConstants.METRIC_NAMESPACE, metricNamespace);
			esCloudTrailPubAccessUrl = pacmanHost + CLOUD_WATCH_LOGS_METRIC_URL;
			resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter, shouldFilter,
					mustTermsFilter, PacmanRuleConstants.FILTER_PATTERN, null);
			if (Objects.isNull(resultSet) || resultSet.isEmpty()) {
				return "Cloudwatch logs with matching conditions does not exists,metricname: " + metricName
						+ ",metricnamespace: " + metricNamespace + ",filtername: " + filterName + ",accountId: "
						+ accountId;
			}
			String filterResponse = resultSet.iterator().next();
			if (!filterResponse.replaceAll("[\\n\t ]", "")
					.equalsIgnoreCase(FilterTypes.valueOf(filterName.toUpperCase()).label.replaceAll("[\\n\t ]", ""))) {
				return "Cloudwatch logs with matching filter patterns does not exists,filtername: " + filterName
						+ ",accountId: " + accountId;
			}

			mustFilter = new HashMap<>();
			mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
			mustFilter.put(PacmanRuleConstants.METRIC_NAME, metricName);
			mustFilter.put(PacmanRuleConstants.NAMESPACE, metricNamespace);
			mustFilter.put(PacmanRuleConstants.METRIC_ACTION_ENABLED, "true");
			esCloudTrailPubAccessUrl = pacmanHost + CLOUD_WATCH_ALARM_URL;
			resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esCloudTrailPubAccessUrl, mustFilter, shouldFilter,
					mustTermsFilter, "_resourceid", null);
			if (Objects.isNull(resultSet) || resultSet.isEmpty()) {
				return "Cloudwatch alarm with matching conditions does not exists,metricname: " + metricName
						+ ",namespace: " + metricNamespace + ",accountId: " + accountId;
			}

		} catch (IllegalArgumentException e) {
			logger.error("Invalid value for filter, filter: " + filterName + e.getMessage(), e);
			description = "Invalid value for filter,filter: " + filterName;
		} catch (Exception ex) {
			logger.error("Cloudwatch alarm not found" + ex.getMessage(), ex);
			description = "Cloudwatch alarm not found";
		}
		return description;
	}

	private static RuleResult buildFailureAnnotation(final Map<String, String> ruleParam, String description) {

		Annotation annotation = null;
		LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();

		annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
		annotation.put(PacmanSdkConstants.DESCRIPTION, description);
		annotation.put(PacmanRuleConstants.SEVERITY, ruleParam.get(PacmanRuleConstants.SEVERITY));
		annotation.put(PacmanRuleConstants.CATEGORY, ruleParam.get(PacmanRuleConstants.CATEGORY));
		annotation.put(PacmanRuleConstants.RESOURCE_ID, ruleParam.get(PacmanRuleConstants.RESOURCE_ID));
		issue.put(PacmanRuleConstants.VIOLATION_REASON, description);
		issueList.add(issue);
		annotation.put("issueDetails", issueList.toString());
		logger.debug("========CheckForLogMetricFilterAndAlarmEnabled annotation {} :=========", annotation);
		return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);

	}

	@Override
	public String getHelpText() {

		return "Checks the CloudTrail log file validation is enabled.";
	}

	private enum FilterTypes {

		UNAUTH_API_CALLS_FILTER(
				"{ ($.errorCode = *UnauthorizedOperation) || ($.errorCode = AccessDenied*) || ($.sourceIPAddress!=delivery.logs.amazonaws.com) || ($.eventName!=HeadBucket) }"),
		MC_SIGNIN_WO_MFA_FILTER(
				"{ ($.eventName = \"ConsoleLogin\") && ($.additionalEventData.MFAUsed != \"Yes\") && ($.userIdentity.type = \"IAMUser\") && ($.responseElements.ConsoleLogin = \"Success\") }"),
		USAGE_OF_ROOT_ACC_FILTER(
				"{ $.userIdentity.type = \"Root\" && $.userIdentity.invokedBy NOT EXISTS && $.eventType != \"AwsServiceEvent\" }"),
		IAM_POL_CHANGES_FILTER(
				"{($.eventName=DeleteGroupPolicy)||($.eventName=DeleteRolePolicy)||($.eventName=DeleteUserPolicy)||($.eventName=PutGroupPolicy)||($.eventName=PutRolePolicy)||($.eventName=PutUserPolicy)||($.eventName=CreatePolicy)||($.eventName=DeletePolicy)||($.eventName=CreatePolicyVersion)||($.eventName=DeletePolicyVersion)||($.eventName=AttachRolePolicy)||($.eventName=DetachRolePolicy)||($.eventName=AttachUserPolicy)||($.eventName=DetachUserPolicy)||($.eventName=AttachGroupPolicy)||($.eventName=DetachGroupPolicy)}"),
		CT_CFG_CHANGES_FILTER(
				"{ ($.eventName = CreateTrail) || ($.eventName = UpdateTrail) || ($.eventName = DeleteTrail) || ($.eventName = StartLogging) || ($.eventName = StopLogging) }"),
		MC_AUTH_FAIL_FILTER("{ ($.eventName = ConsoleLogin) && ($.errorMessage = \"Failed authentication\") }"),
		CUST_CMK_CHANGES_FILTER(
				"{($.eventSource = kms.amazonaws.com) && (($.eventName=DisableKey)||($.eventName=ScheduleKeyDeletion)) }"),
		S3_BUCK_CHANGES_FILTER(
				"{ ($.eventSource = s3.amazonaws.com) && (($.eventName = PutBucketAcl) || ($.eventName = PutBucketPolicy) || ($.eventName = PutBucketCors) || ($.eventName = PutBucketLifecycle) || ($.eventName = PutBucketReplication) || ($.eventName = DeleteBucketPolicy) || ($.eventName = DeleteBucketCors) || ($.eventName = DeleteBucketLifecycle) || ($.eventName = DeleteBucketReplication)) }"),
		AWS_CONF_CHANGES_FILTER(
				"{ ($.eventSource = config.amazonaws.com) && (($.eventName=StopConfigurationRecorder)||($.eventName=DeleteDeliveryChannel)||($.eventName=PutDeliveryChannel)||($.eventName=PutConfigurationRecorder)) }"),
		SEC_GRP_CHANGES_FILTER(
				"{ ($.eventName = AuthorizeSecurityGroupIngress) || ($.eventName = AuthorizeSecurityGroupEgress) || ($.eventName = RevokeSecurityGroupIngress) || ($.eventName = RevokeSecurityGroupEgress) || ($.eventName = CreateSecurityGroup) || ($.eventName = DeleteSecurityGroup) }"),
		NACL_CHANGES_FILTER(
				"{ ($.eventName = CreateNetworkAcl) || ($.eventName = CreateNetworkAclEntry) || ($.eventName = DeleteNetworkAcl) || ($.eventName = DeleteNetworkAclEntry) || ($.eventName = ReplaceNetworkAclEntry) || ($.eventName = ReplaceNetworkAclAssociation) }"),
		NTWK_GTWY_CHANGES_FILTER(
				"{ ($.eventName = CreateCustomerGateway) || ($.eventName = DeleteCustomerGateway) || ($.eventName = AttachInternetGateway) || ($.eventName = CreateInternetGateway) || ($.eventName = DeleteInternetGateway) || ($.eventName = DetachInternetGateway) }"),
		ROUTE_TBL_CHANGES_FILTER(
				"{ ($.eventName = CreateRoute) || ($.eventName = CreateRouteTable) || ($.eventName = ReplaceRoute) || ($.eventName = ReplaceRouteTableAssociation) || ($.eventName = DeleteRouteTable) || ($.eventName = DeleteRoute) || ($.eventName = DisassociateRouteTable) }"),
		VPC_CHANGES_FILTER(
				"{ ($.eventName = CreateVpc) || ($.eventName = DeleteVpc) || ($.eventName = ModifyVpcAttribute) || ($.eventName = AcceptVpcPeeringConnection) || ($.eventName = CreateVpcPeeringConnection) || ($.eventName = DeleteVpcPeeringConnection) || ($.eventName = RejectVpcPeeringConnection) || ($.eventName = AttachClassicLinkVpc) || ($.eventName = DetachClassicLinkVpc) || ($.eventName = DisableVpcClassicLink) || ($.eventName = EnableVpcClassicLink) }"),
		AWS_ORG_CHANGES_FILTER(
				"{ ($.eventSource = organizations.amazonaws.com) && (($.eventName = \"AcceptHandshake\") || ($.eventName = \"AttachPolicy\") || ($.eventName = \"CreateAccount\") || ($.eventName = \"CreateOrganizationalUnit\") || ($.eventName = \"CreatePolicy\") || ($.eventName = \"DeclineHandshake\") || ($.eventName = \"DeleteOrganization\") || ($.eventName = \"DeleteOrganizationalUnit\") || ($.eventName = \"DeletePolicy\") || ($.eventName = \"DetachPolicy\") || ($.eventName = \"DisablePolicyType\") || ($.eventName = \"EnablePolicyType\") || ($.eventName = \"InviteAccountToOrganization\") || ($.eventName = \"LeaveOrganization\") || ($.eventName = \"MoveAccount\") || ($.eventName = \"RemoveAccountFromOrganization\") || ($.eventName = \"UpdatePolicy\") || ($.eventName = \"UpdateOrganizationalUnit\")) }"),;

		private FilterTypes(String label) {
			this.label = label;
		}

		public final String label;
	}

}
