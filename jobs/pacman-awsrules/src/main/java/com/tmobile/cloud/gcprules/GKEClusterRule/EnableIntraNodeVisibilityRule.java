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

@PacmanRule(key = "check-intranode-visibility",desc = "Enable VPC Flow Logs and Intranode Visibility to see pod-level traffic, even for traffic within a worker node.", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class EnableIntraNodeVisibilityRule extends BaseRule  {

    private static final Logger logger = LoggerFactory.getLogger(EnableIntraNodeVisibilityRule.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String violationReason = ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        String description = ruleParam.get(PacmanRuleConstants.DESCRIPTION);
        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_gkecluster/_search";
        }
        logger.debug("========gcp_gkecluster URL after concatenation param {}  =========", vmEsURL);
        boolean isEnabled = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isEnabled = checkIntraNodeVisibilityEnabled(vmEsURL, mustFilter);
                if (!isEnabled) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, description);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========rule ended with status failure {}", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("======== ended with status true=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkIntraNodeVisibilityEnabled(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========verifyports  started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            logger.debug("========verifyports hit array=========");

            JsonObject gkeCluster = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", gkeCluster);

            boolean intraNodeVisibility=gkeCluster.getAsJsonObject().get(PacmanRuleConstants.INTRANODE_VISIBILITY).getAsBoolean();
            if(intraNodeVisibility){
                validationResult=true;
            }
        }
        else {
            logger.debug(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return validationResult;
    }

        @Override
        public String getHelpText () {
            return "This rule checks if VPC Flow Logs and IntraNode Visibility to see pod-level traffic is enabled or not";
        }
   }

