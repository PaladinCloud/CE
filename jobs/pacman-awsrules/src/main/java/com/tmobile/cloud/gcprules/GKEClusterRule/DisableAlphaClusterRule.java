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
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
@PacmanPolicy(key = "disable-alpha-clusters", desc = "disable alpha clusters for production workloads", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class DisableAlphaClusterRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(DisableAlphaClusterRule.class);
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);

        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String violationReason=ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        String description=ruleParam.get(PacmanRuleConstants.DESCRIPTION);
        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_gkecluster/_search";
        }
        logger.debug("========gcp_gkecluster URL after concatenation param {}  =========", vmEsURL);
        boolean isKeyEnabled = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isKeyEnabled = checkIfAlphaClusterDisabled(vmEsURL, mustFilter);
                if (!isKeyEnabled) {
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
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("======== ended with status true=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkIfAlphaClusterDisabled(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========checkIfAlphaClusterDisabled  started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = true;
        if (hitsJsonArray.size() > 0) {
            logger.debug("========checkIfAlphaClusterDisabled hit array=========");

            JsonObject gkeCluster = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", gkeCluster);

            boolean enableKubernetesAlpha=gkeCluster.getAsJsonPrimitive("enableKubernetesAlpha").getAsBoolean();
            if(enableKubernetesAlpha){
               validationResult=false;
            }

        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Disable Alpha clusters for Production Workloads";
    }
}
