package com.tmobile.cloud.gcprules.GKEClusterRule;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

@PacmanRule(key = "check-node-encryption-access-for-gke-cluster", desc = "Enable GKE Cluster Node Encryption with Customer-Managed Keys", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class CMKNodeEncryptionRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(CMKNodeEncryptionRule.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

        logger.debug("Executing encryption with customer-managed keys rule.");
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String description = ruleParam.get(PacmanRuleConstants.DESCRIPTION);
        String violationReason = ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String esEndpoint = ruleParam.get(PacmanRuleConstants.ES_SG_RULES_URL);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, esUrl))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(esUrl)) {
            esUrl = esUrl + esEndpoint;
        }
        logger.debug("ES search url :  {}", esUrl);
        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put(PacmanSdkConstants.RULE_ID, ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                if (checkEncryptionKeyNull(esUrl, mustFilter)) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                    Annotation annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, description);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                    logger.debug("GKE Cluster Node Encryption with customer-managed key rule ended with failure. Annotation {} :", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("GKE Cluster Node Encryption with customer-managed key rule ended with success.");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkEncryptionKeyNull(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.debug("Validating customer managed key encryption rule");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(esUrl, mustFilter);
        boolean validationResult =false;
        if (hitsJsonArray.size() > 0) {
            JsonObject gkeCluster = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
            logger.debug("Validating the data item: {}", gkeCluster.toString());

            String bootDiskKmsKey = gkeCluster.getAsJsonObject()
                    .get(PacmanRuleConstants.BOOTDISKKMSKEY).getAsJsonObject().toString();
            if (bootDiskKmsKey!=null) {
                validationResult = true;
            }
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if the GKE cluster Node encryption is enabled with cmk.";
    }
}
