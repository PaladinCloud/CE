package com.tmobile.cloud.gcprules.cloudfunctions;

import com.amazonaws.util.StringUtils;
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

import java.util.*;

@PacmanPolicy(key = "GCP-Cloud-Function-configured-with-overly-permissive-Ingress-setting", desc = "GCP Cloud Function configured with overly permissive Ingress setting", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class IngressSettingRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(IngressSettingRule.class);
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("Executing GCP-Cloud-Function-configured-with-overly-permissive-Ingress-setting rule for gcp cloud function");
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String esUrl = GCPUtils.getEsUrl(ruleParam);
        if (Boolean.FALSE.equals(GCPUtils.validateRuleParam(ruleParam))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        logger.debug("========gcp_cloudfunction URL after concatenation param {}  =========", esUrl);
        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put(PacmanSdkConstants.POLICY_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");
            try {
                boolean isOverlyPermissiveIngress = checkIngressSettings(esUrl, resourceId);
                if (!isOverlyPermissiveIngress) {
                    return GCPUtils.fetchPolicyResult(ruleParam, PacmanRuleDescriptionConstants.INGRESS_SETTING,
                            PacmanRuleViolationReasonConstants.INGRESS_SETTING);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("======== ended with status true=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }
    private boolean checkIngressSettings(String vmEsURL, String resourceId) throws Exception {
        logger.debug("========checkIngressSettings  started=========");
        boolean validationResult = false;
        JsonObject sourceData = GCPUtils.getJsonObjFromSourceData(vmEsURL, resourceId);
        if(sourceData != null){
            String ingressSetting  = !sourceData.getAsJsonObject().get(PacmanRuleConstants.INGRESS_SETTING).isJsonNull() ?
                    sourceData.getAsJsonObject().get(PacmanRuleConstants.INGRESS_SETTING).getAsString() : "";
            if(!StringUtils.isNullOrEmpty(ingressSetting) && !ingressSetting.equals(PacmanRuleConstants.ALLOW_ALL)){
                validationResult = true;
            }
            logger.debug("Validating the data item: {}", sourceData);
        }
        return validationResult;
    }


    @Override
    public String getHelpText() {
        return "GCP Cloud Functions are configured with overly permissive Ingress setting";
    }
}
