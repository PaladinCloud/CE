package com.tmobile.cloud.ocirules.tenancy;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CIS OCI Foundations Benchmark v3.1.1 - 4.2 Create at least one notification topic and subscription to receive monitoring alerts.
 * Runs on the tenancy asset and fails when no ACTIVE notification topic of the tenancy has an ACTIVE subscription.
 */
@PacmanPolicy(key = "check-oci-tenancy-notification-topic-with-subscription", desc = "checks if OCI tenancy has at least one notification topic with an active subscription", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class TenancyNotificationSubscriptionExistsRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(TenancyNotificationSubscriptionExistsRule.class);
    private static final String ES_TOPIC_URL = "esOnsTopicUrl";
    private static final String ES_SUBSCRIPTION_URL = "esOnsSubscriptionUrl";
    private static final String TOPIC_ID = "topicId";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========TenancyNotificationSubscriptionExistsRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String tenancyId = OciTenancyAssetUtil.getTenancyId(resourceAttributes);
        if (StringUtils.isNullOrEmpty(tenancyId)) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
        }

        List<JsonObject> topics;
        List<JsonObject> subscriptions;
        try {
            topics = OciTenancyAssetUtil.getLatestAssets(ruleParam, ES_TOPIC_URL, tenancyId);
            subscriptions = OciTenancyAssetUtil.getLatestAssets(ruleParam, ES_SUBSCRIPTION_URL, tenancyId);
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            logger.error("unable to fetch OCI notification topics or subscriptions of tenancy {}", tenancyId, e);
            throw new RuleExecutionFailedExeption("unable to fetch OCI notification topics or subscriptions " + e);
        }

        Set<String> activeTopicIds = new HashSet<>();
        for (JsonObject topic : topics) {
            String topicId = OciTenancyAssetUtil.getString(topic, TOPIC_ID);
            if (!StringUtils.isNullOrEmpty(topicId) && OciTenancyAssetUtil.isActive(topic)) {
                activeTopicIds.add(topicId);
            }
        }

        // Pass when at least one ACTIVE subscription belongs to an ACTIVE topic
        int activeSubscriptions = 0;
        for (JsonObject subscription : subscriptions) {
            if (OciTenancyAssetUtil.isActive(subscription)
                    && activeTopicIds.contains(OciTenancyAssetUtil.getString(subscription, TOPIC_ID))) {
                activeSubscriptions++;
            }
        }

        if (activeSubscriptions == 0) {
            String violationReason = activeTopicIds.isEmpty()
                    ? "OCI tenancy has no active notification topic to receive monitoring alerts"
                    : "OCI tenancy has no notification topic with an active subscription to receive monitoring alerts";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issue.put("activeTopics", activeTopicIds.size());
            issue.put("subscriptionsFound", subscriptions.size());
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========TenancyNotificationSubscriptionExistsRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========TenancyNotificationSubscriptionExistsRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    @Override
    public String getHelpText() {
        return "Checks that the OCI tenancy has at least one notification topic with an active subscription";
    }
}
