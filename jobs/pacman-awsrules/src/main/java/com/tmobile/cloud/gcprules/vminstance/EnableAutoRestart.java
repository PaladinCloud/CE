package com.tmobile.cloud.gcprules.vminstance;

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

@PacmanPolicy(key = "enable-auto-restart-for-vm-instance", desc = "Enable Automatic Restart for VM Instances", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class EnableAutoRestart extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(EnableAutoRestart.class);

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========EnableAutoRestart started=========");

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        if (Boolean.FALSE.equals(GCPUtils.validateRuleParam(ruleParam))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String vmEsURL = GCPUtils.getEsUrl(ruleParam);
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put(PacmanSdkConstants.POLICY_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            try {
                boolean ifVmHas2FA = verifyAutoRestart(vmEsURL, resourceId);
                if (!ifVmHas2FA) {
                    return GCPUtils.fetchPolicyResult(ruleParam, PacmanRuleDescriptionConstants.ENABLE_AUTO_RESTART,
                            PacmanRuleViolationReasonConstants.ENABLE_AUTO_RESTART);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("========EnableAutoRestart ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean verifyAutoRestart(String vmEsURL, String resourceId) throws Exception {
        logger.debug("========verifyAutoRestart started=========");
        JsonObject vmInstanceObject = GCPUtils.getJsonObjFromSourceData(vmEsURL, resourceId);
        if(vmInstanceObject != null && vmInstanceObject.getAsJsonObject() != null &&
                !vmInstanceObject.getAsJsonObject().get(PacmanRuleConstants.HAS_AUTO_RESTART).isJsonNull()){
            return vmInstanceObject.getAsJsonObject()
                    .get(PacmanRuleConstants.HAS_AUTO_RESTART).getAsBoolean();
        }
        logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        return false;
    }

    @Override
    public String getHelpText() {
        return "This rule checks  if the vm instance have enable auto restart as true";
    }
}
