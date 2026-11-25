package com.tmobile.cloud.ocirules.network;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CIS OCI Foundations Benchmark v3.1.1 - 2.1 Ensure no security lists allow ingress from 0.0.0.0/0 to port 22
 * and 2.2 Ensure no security lists allow ingress from 0.0.0.0/0 to port 3389.
 * The port to check comes from the policy param "port", so the same policyKey is used for both policies.
 */
@PacmanPolicy(key = "check-oci-security-list-ingress-port-open-to-internet", desc = "checks if OCI security list allows ingress from 0.0.0.0/0 to the configured port", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class SecurityListIngressPortRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(SecurityListIngressPortRule.class);
    private static final String INGRESS_SECURITY_RULES = "ingressSecurityRules";
    private static final String PORT = "port";
    private static final String ANY_SOURCE = "0.0.0.0/0";
    private static final String PROTOCOL_TCP = "6";
    private static final String PROTOCOL_ALL = "all";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========SecurityListIngressPortRule started=========");
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String port = ruleParam.get(PORT);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, port))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        int portNumber;
        try {
            portNumber = Integer.parseInt(port.trim());
        } catch (NumberFormatException e) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String ingressSecurityRules = resourceAttributes.get(INGRESS_SECURITY_RULES);
        if (StringUtils.isNullOrEmpty(ingressSecurityRules)) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            logger.debug("========SecurityListIngressPortRule ended=========");
            return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
        }

        // Rule fails when an ingress rule has source 0.0.0.0/0 and protocol all, or protocol TCP with no
        // destination port range (all ports) or a destination port range that includes the port
        List<String> violatingRules = new ArrayList<>();
        for (JsonElement element : parseRules(ingressSecurityRules)) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject rule = element.getAsJsonObject();
            String source = getString(rule, "source");
            String protocol = getString(rule, "protocol");
            if (!ANY_SOURCE.equals(source)) {
                continue;
            }
            if (PROTOCOL_ALL.equalsIgnoreCase(protocol)) {
                violatingRules.add("source " + source + ", protocol all, all ports");
            } else if (PROTOCOL_TCP.equals(protocol)) {
                JsonObject portRange = getDestinationPortRange(rule);
                if (portRange == null) {
                    violatingRules.add("source " + source + ", protocol TCP, all ports");
                } else if (portRange.has("min") && portRange.has("max")
                        && portRange.get("min").getAsInt() <= portNumber && portNumber <= portRange.get("max").getAsInt()) {
                    violatingRules.add("source " + source + ", protocol TCP, ports " + portRange.get("min").getAsInt() + "-" + portRange.get("max").getAsInt());
                }
            }
        }

        if (!violatingRules.isEmpty()) {
            String violationReason = "OCI security list allows ingress from 0.0.0.0/0 to port " + portNumber;
            List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
            LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

            Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
            annotation.put(PacmanSdkConstants.DESCRIPTION, violationReason);
            annotation.put(PacmanRuleConstants.SEVERITY, severity);
            annotation.put(PacmanRuleConstants.CATEGORY, category);
            issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
            issue.put(INGRESS_SECURITY_RULES, String.join("; ", violatingRules));
            issueList.add(issue);
            annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
            logger.debug("========SecurityListIngressPortRule ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
        }

        logger.debug("========SecurityListIngressPortRule ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    /**
     * The rule engine passes list attributes as a JSON array string, e.g. [{"protocol":"6","source":"0.0.0.0/0",...}].
     */
    private static JsonArray parseRules(String ingressSecurityRules) {
        try {
            JsonElement parsed = JsonParser.parseString(ingressSecurityRules);
            return parsed.isJsonArray() ? parsed.getAsJsonArray() : new JsonArray();
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            return new JsonArray();
        }
    }

    private static JsonObject getDestinationPortRange(JsonObject rule) {
        JsonElement tcpOptions = rule.get("tcpOptions");
        if (tcpOptions == null || !tcpOptions.isJsonObject()) {
            return null;
        }
        JsonElement portRange = tcpOptions.getAsJsonObject().get("destinationPortRange");
        return portRange != null && portRange.isJsonObject() ? portRange.getAsJsonObject() : null;
    }

    private static String getString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && !value.isJsonNull() ? value.getAsString().trim() : null;
    }

    @Override
    public String getHelpText() {
        return "Checks that OCI security lists do not allow ingress from 0.0.0.0/0 to the configured port";
    }
}
