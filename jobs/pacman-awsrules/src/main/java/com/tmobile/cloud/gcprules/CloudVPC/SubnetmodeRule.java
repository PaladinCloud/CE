package com.tmobile.cloud.gcprules.CloudVPC;

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

@PacmanRule(key = "check-subnet-mode-is-legacy-vpc", desc = "Ensure legacy subnet mode VPC not being used in the project", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class SubnetmodeRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(SubnetmodeRule.class);
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("========== Ensure legacy subnet mode VPC not being used in the project =========== ");

        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        //EsUrl will be changed for sqlserver db instance.
        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_networks/_search";
        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);
        boolean isLegacyVPC = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isLegacyVPC = isLegacyNetwork(vmEsURL, mustFilter);
                if (isLegacyVPC) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Check if the legacy Virtual Private Cloud  is being used within  GCP projects");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "VPC is using legacy VPC for GCP projects");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========Vpc legacy Rule ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("========Vpc legacy Rule ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean isLegacyNetwork(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("======== Vpc legacy Rule started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            JsonObject dbinstances = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {} and size(){}", dbinstances,hitsJsonArray.size());
            boolean autoCreateSubnetworks = dbinstances.getAsJsonObject()
                    .get(PacmanRuleConstants.autoCreateSubnetworks).getAsBoolean();
            logger.info("************* default {}",autoCreateSubnetworks);

            if(autoCreateSubnetworks){
                validationResult=true;
            }


        }  else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Sub net mode should not be legacy";
    }
}
