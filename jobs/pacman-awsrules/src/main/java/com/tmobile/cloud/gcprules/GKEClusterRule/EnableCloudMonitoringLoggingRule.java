package com.tmobile.cloud.gcprules.GKEClusterRule;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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


@PacmanPolicy(key = "enable-cloud-logging-monitoring", desc = "Enable Cloud Logging Monitoring", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class EnableCloudMonitoringLoggingRule extends BasePolicy {
    private static final Logger logger= LoggerFactory.getLogger(EnableCloudMonitoringLoggingRule.class);
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);

        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_gkecluster/_search";
        }
        logger.debug("========gcp_gkecluster URL after concatenation param {}  =========", vmEsURL);
        boolean isCloudMonitoringLoggingEnabled = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isCloudMonitoringLoggingEnabled = checkCloudMonitoringLoggingEnabled(vmEsURL, mustFilter);
                if (!isCloudMonitoringLoggingEnabled) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Enabling cloud logging and monitoring Send logs and metrics to a remote aggregator to mitigate the risk of local tampering in the event of a breach.");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "Cloud Logging and Monitoring is disabled");
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
    private boolean checkCloudMonitoringLoggingEnabled(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========verifyports  started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            logger.debug("========verifyports hit array=========");

            JsonObject gkeCluster = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
            logger.debug("Validating the data item: {}", gkeCluster);
            if(gkeCluster.get(PacmanRuleConstants.CLOUD_LOGGING) !=null&& gkeCluster.get(PacmanRuleConstants.CLOUD_MONITORING)!=null&&gkeCluster.get(PacmanRuleConstants.CLOUD_LOGGING).getAsString().equalsIgnoreCase(PacmanRuleConstants.CLOUD_LOGGING_VALUE) && gkeCluster.get(PacmanRuleConstants.CLOUD_MONITORING).getAsString().equalsIgnoreCase(PacmanRuleConstants.CLOUD_MONITORING_VALUE)){
                validationResult=true;
            }




        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Enable Cloud logging and Monitoring";
    }
}
