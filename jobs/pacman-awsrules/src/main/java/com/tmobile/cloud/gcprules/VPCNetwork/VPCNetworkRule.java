package com.tmobile.cloud.gcprules.VPCNetwork;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.utils.GCPFirewallUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tmobile.cloud.constants.PacmanRuleConstants.DELIMITER_COMA;

@PacmanPolicy(key = "check-for-vpc-network-firewall-security", desc = "checks for vpc network public IP address", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class VPCNetworkRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(VPCNetworkRule.class);
    private static final String VM_ES_URL = "/gcp_vpcfirewall/_search";
    private static final String RESOURCE_ID_EMPTY_MSG = "Resource id is empty";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String[] port = ruleParam.get(PacmanRuleConstants.PORT).split(DELIMITER_COMA);
        String description = ruleParam.get(PacmanRuleConstants.DESCRIPTION);
        String violationReason = ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (StringUtils.isNotEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + VM_ES_URL;
        }
        logger.debug("========gcp_vpcfirewall URL after concatenation param {}  =========", vmEsURL);

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
        LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

        Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
        annotation.put(PacmanSdkConstants.DESCRIPTION, description);
        annotation.put(PacmanRuleConstants.SEVERITY, severity);
        annotation.put(PacmanRuleConstants.CATEGORY, category);
        annotation.put(PacmanRuleConstants.FIREWALL_RULE_NAME, resourceAttributes.get(PacmanRuleConstants.NAME));

        if (StringUtils.isEmpty(resourceId)) {
            issue.put(PacmanRuleConstants.VIOLATION_REASON, RESOURCE_ID_EMPTY_MSG);
            issueList.add(issue);
            annotation.put("issueDetails", issueList.toString());
            logger.debug("========VPCNetworkInboundPolicy ended with an annotation {} : =========", annotation);
            return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                    annotation);
        }

        logger.debug("========after url");
        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
        mustFilter.put(PacmanRuleConstants.LATEST, true);

        try {
            if (!GCPFirewallUtils.verifyPorts(vmEsURL, mustFilter, port, PacmanRuleConstants.INGRESS)) {
                issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
                issueList.add(issue);
                annotation.put("issueDetails", issueList.toString());
                logger.debug("========VPCNetworkInboundPolicy ended with an annotation {} : =========", annotation);
                return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }

        } catch (Exception exception) {
            throw new RuleExecutionFailedExeption(exception.getMessage());
        }
        logger.debug("========VPCNetworkInboundPolicy ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    @Override
    public String getHelpText() {
        return "check public Access to VPC Fire wall";
    }

}
