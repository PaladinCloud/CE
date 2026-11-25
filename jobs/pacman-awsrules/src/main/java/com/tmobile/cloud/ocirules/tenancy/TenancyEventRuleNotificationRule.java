package com.tmobile.cloud.ocirules.tenancy;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * CIS OCI Foundations Benchmark v3.1.1 - 4.3 to 4.12, 4.15 and 4.18 Ensure a notification is configured for ... changes.
 * Runs on the tenancy asset. The event types to watch come from the policy param "eventTypes" (comma separated), so the
 * same policyKey is used by every notification policy. The rule passes when the event types are covered by event rules
 * in the root compartment that are ACTIVE and enabled and have an enabled Notifications (ONS) action whose topic is
 * ACTIVE and has at least one ACTIVE subscription. The event types may be split across several such rules.
 */
@PacmanPolicy(key = "check-oci-tenancy-event-rule-notification", desc = "checks if OCI tenancy has a root compartment event rule that sends the configured event types to a notification topic", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class TenancyEventRuleNotificationRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(TenancyEventRuleNotificationRule.class);
    private static final String EVENT_TYPES = "eventTypes";
    private static final String ES_EVENT_RULE_URL = "esEventRuleUrl";
    private static final String ES_TOPIC_URL = "esOnsTopicUrl";
    private static final String ES_SUBSCRIPTION_URL = "esOnsSubscriptionUrl";
    private static final String TOPIC_ID = "topicId";
    private static final String IS_ENABLED = "isEnabled";
    private static final String CONDITION = "condition";
    private static final String EVENT_TYPE = "eventType";
    private static final String ACTIONS = "actions";
    private static final String ACTION_TYPE = "actionType";
    private static final String ONS = "ONS";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========TenancyEventRuleNotificationRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        Set<String> requiredEventTypes = parseEventTypes(ruleParam.get(EVENT_TYPES));
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category)) || requiredEventTypes.isEmpty()) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String tenancyId = OciTenancyAssetUtil.getTenancyId(resourceAttributes);
        if (StringUtils.isNullOrEmpty(tenancyId)) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
        }

        List<JsonObject> eventRules;
        List<JsonObject> topics;
        List<JsonObject> subscriptions;
        try {
            eventRules = OciTenancyAssetUtil.getLatestAssets(ruleParam, ES_EVENT_RULE_URL, tenancyId);
            topics = OciTenancyAssetUtil.getLatestAssets(ruleParam, ES_TOPIC_URL, tenancyId);
            subscriptions = OciTenancyAssetUtil.getLatestAssets(ruleParam, ES_SUBSCRIPTION_URL, tenancyId);
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            logger.error("unable to fetch OCI event rules, notification topics or subscriptions of tenancy {}", tenancyId, e);
            throw new RuleExecutionFailedExeption("unable to fetch OCI event rules, notification topics or subscriptions " + e);
        }

        Set<String> notifyingTopicIds = getNotifyingTopicIds(topics, subscriptions);

        // Collect the event types of root compartment rules that are ACTIVE, enabled and notify a topic that reaches someone
        Set<String> coveredEventTypes = new HashSet<>();
        int rootEventRules = 0;
        int notifyingRootEventRules = 0;
        for (JsonObject eventRule : eventRules) {
            if (!tenancyId.equals(OciTenancyAssetUtil.getString(eventRule, OciTenancyAssetUtil.COMPARTMENT_ID))) {
                continue;
            }
            rootEventRules++;
            if (OciTenancyAssetUtil.isActive(eventRule) && isEnabled(eventRule) && hasNotificationAction(eventRule, notifyingTopicIds)) {
                notifyingRootEventRules++;
                coveredEventTypes.addAll(getConditionEventTypes(eventRule));
            }
        }

        Set<String> missingEventTypes = new LinkedHashSet<>(requiredEventTypes);
        missingEventTypes.removeAll(coveredEventTypes);
        if (!missingEventTypes.isEmpty()) {
            String violationReason = "OCI tenancy has no active root compartment event rule that sends the required event types to a notification topic with an active subscription";
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issue.put("missingEventTypes", String.join("; ", missingEventTypes));
            issue.put("rootEventRules", rootEventRules);
            issue.put("notifyingRootEventRules", notifyingRootEventRules);
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========TenancyEventRuleNotificationRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========TenancyEventRuleNotificationRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private static Set<String> parseEventTypes(String eventTypes) {
        Set<String> result = new LinkedHashSet<>();
        if (!StringUtils.isNullOrEmpty(eventTypes)) {
            for (String eventType : eventTypes.split(",")) {
                if (!eventType.trim().isEmpty()) {
                    result.add(eventType.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return result;
    }

    /**
     * Topics that are ACTIVE and have at least one ACTIVE subscription, so a notification reaches someone.
     */
    private static Set<String> getNotifyingTopicIds(List<JsonObject> topics, List<JsonObject> subscriptions) {
        Set<String> activeTopicIds = new HashSet<>();
        for (JsonObject topic : topics) {
            String topicId = OciTenancyAssetUtil.getString(topic, TOPIC_ID);
            if (!StringUtils.isNullOrEmpty(topicId) && OciTenancyAssetUtil.isActive(topic)) {
                activeTopicIds.add(topicId);
            }
        }
        Set<String> notifyingTopicIds = new HashSet<>();
        for (JsonObject subscription : subscriptions) {
            String topicId = OciTenancyAssetUtil.getString(subscription, TOPIC_ID);
            if (OciTenancyAssetUtil.isActive(subscription) && activeTopicIds.contains(topicId)) {
                notifyingTopicIds.add(topicId);
            }
        }
        return notifyingTopicIds;
    }

    private static boolean isEnabled(JsonObject object) {
        return "true".equalsIgnoreCase(OciTenancyAssetUtil.getString(object, IS_ENABLED));
    }

    /**
     * The collector stores the GetRule actions as {"actions": {"actions": [{"actionType": "ONS", "topicId": ...}]}}.
     */
    private static boolean hasNotificationAction(JsonObject eventRule, Set<String> notifyingTopicIds) {
        JsonElement actions = eventRule.get(ACTIONS);
        if (actions != null && actions.isJsonObject()) {
            actions = actions.getAsJsonObject().get(ACTIONS);
        }
        if (actions == null || !actions.isJsonArray()) {
            return false;
        }
        for (JsonElement element : actions.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject action = element.getAsJsonObject();
            String lifecycleState = OciTenancyAssetUtil.getString(action, OciTenancyAssetUtil.LIFECYCLE_STATE);
            if (ONS.equalsIgnoreCase(OciTenancyAssetUtil.getString(action, ACTION_TYPE))
                    && !"false".equalsIgnoreCase(OciTenancyAssetUtil.getString(action, IS_ENABLED))
                    && (lifecycleState == null || OciTenancyAssetUtil.ACTIVE.equalsIgnoreCase(lifecycleState))
                    && notifyingTopicIds.contains(OciTenancyAssetUtil.getString(action, TOPIC_ID))) {
                return true;
            }
        }
        return false;
    }

    /**
     * The rule condition is a JSON string such as {"eventType":["com.oraclecloud.virtualnetwork.createvcn", ...],"data":{}}.
     * eventType can be a single string or a list.
     */
    private static Set<String> getConditionEventTypes(JsonObject eventRule) {
        Set<String> result = new HashSet<>();
        JsonElement condition = eventRule.get(CONDITION);
        JsonObject conditionJson = null;
        try {
            if (condition != null && condition.isJsonPrimitive()) {
                JsonElement parsed = JsonParser.parseString(condition.getAsString());
                conditionJson = parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
            } else if (condition != null && condition.isJsonObject()) {
                conditionJson = condition.getAsJsonObject();
            }
        } catch (RuntimeException e) {
            logger.info("unable to parse event rule condition {}", condition);
        }
        if (conditionJson == null || !conditionJson.has(EVENT_TYPE)) {
            return result;
        }
        JsonElement eventType = conditionJson.get(EVENT_TYPE);
        if (eventType.isJsonArray()) {
            JsonArray array = eventType.getAsJsonArray();
            for (JsonElement value : array) {
                if (value.isJsonPrimitive()) {
                    result.add(value.getAsString().trim().toLowerCase(Locale.ROOT));
                }
            }
        } else if (eventType.isJsonPrimitive()) {
            result.add(eventType.getAsString().trim().toLowerCase(Locale.ROOT));
        }
        return result;
    }

    @Override
    public String getHelpText() {
        return "Checks that the OCI tenancy has an active root compartment event rule that sends the configured event types to a notification topic";
    }
}
