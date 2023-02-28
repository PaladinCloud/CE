package com.tmobile.cloud.gcprules.InstanceGroup;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.constants.PacmanRuleDescriptionConstants;
import com.tmobile.cloud.constants.PacmanRuleViolationReasonConstants;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

@PacmanPolicy(key = "Enable-Autoheal-for-instance-group", desc = "Ensure that your Google Cloud Managed Instance Groups (MIGs) are configured with Autohealing feature", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class EnableAutoHealRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(EnableAutoHealRule.class);

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("ExecutingEnable-Autoheal-for-instance-group rule for gcp cloud function");
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String esUrl = GCPUtils.getEsUrl(ruleParam);
        if (Boolean.FALSE.equals(GCPUtils.validateRuleParam(ruleParam))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        logger.debug("========gcp_instancegroup URL after concatenation param {}  =========", esUrl);
        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put(PacmanSdkConstants.POLICY_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");
            try {
                boolean isAutoHealEnabled = checkIfAutoHealEnabled(esUrl, resourceId);
                if (!isAutoHealEnabled) {
                    return GCPUtils.fetchPolicyResult(ruleParam, PacmanRuleDescriptionConstants.HTTP_TRIGGERS,
                            PacmanRuleViolationReasonConstants.HTTP_TRIGGERS);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("======== ended with status true=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkIfAutoHealEnabled(String vmEsURL, String resourceId) throws Exception {
        logger.debug("========checkIfAutoHealEnabled      started=========");
        boolean validationResult = true;
        JsonObject sourceData = GCPUtils.getJsonObjFromSourceData(vmEsURL, resourceId);
        if(sourceData != null){
            JsonArray js = sourceData.get("managedInstance").getAsJsonArray();
            for(int i = 0;i<js.size();i++){
                Integer healthCheckCount = js.get(i).getAsJsonObject().get("instanceHealthCount").getAsInt();
                if(healthCheckCount == 0){
                    validationResult = false;
                }
            }
            logger.debug("Validating the data item: {}", sourceData);
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "GCP Cloud Function HTTP trigger is not secured";
    }
}
