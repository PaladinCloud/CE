package com.tmobile.cloud.gcprules.vminstance;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

@PacmanPolicy(key = "disable-auto-delete-for-persistent-disk", desc = "Disable auto delete for persistent disk", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class DisableAutoDeleteVMPersistentDisk extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(DisableAutoDeleteVMPersistentDisk.class);

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========Disable auto delete for vm persistent disk started=========");
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
                boolean ifDiskHashAutoDelete = verifyAutoDeleteForPersistentDisk(vmEsURL, resourceId);
                if (!ifDiskHashAutoDelete) {
                    return GCPUtils.fetchPolicyResult(ruleParam, PacmanRuleDescriptionConstants.AUTO_DELETE_PERSISTENT_DISK,
                            PacmanRuleViolationReasonConstants.AUTO_DELETE_PERSISTENT_DISK);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("========EnableAutoRestart ended=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean verifyAutoDeleteForPersistentDisk(String vmEsURL, String resourceId) throws Exception {
        logger.debug("========verifyAutoRestart started=========");
        boolean validationResult = true;
        JsonObject vmInstanceObject = GCPUtils.getJsonObjFromSourceData(vmEsURL, resourceId);
        if(vmInstanceObject != null){
            JsonArray disks = vmInstanceObject.get(PacmanRuleConstants.DISKS).getAsJsonArray();
            for(JsonElement disk : disks){
                if((disk.getAsJsonObject().get(PacmanRuleConstants.TYPE) != null) &&
                        (disk.getAsJsonObject().get(PacmanRuleConstants.TYPE).getAsString().equals(PacmanRuleConstants.PERSISTENT)) && (
                        (disk.getAsJsonObject().get(PacmanRuleConstants.AUTO_DELETE) == null ) || (disk.getAsJsonObject().get(PacmanRuleConstants.AUTO_DELETE) != null &&
                                disk.getAsJsonObject().get(PacmanRuleConstants.AUTO_DELETE).getAsBoolean()))){
                    validationResult = false;
                    break;
                }
            }
        }else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if the persistent disk have auto delete as true";
    }
}
