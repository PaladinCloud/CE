package com.tmobile.cloud.gcprules.iam;

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
@PacmanPolicy(key = "enforce-separation-of-duties-for-kms", desc = "Enforce Separation of Duties While Assigning KMS Related Roles to Users", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class SeparationOfDutiesForKMSRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(SeparationOfDutiesForKMSRule.class);
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("executing SeparationOfDutiesForKMSRule....");
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
            vmEsURL = vmEsURL + "/gcp_iamusers/_search";
        }
        logger.debug("========gcp_iamusers URL after concatenation param {}  =========", vmEsURL);

        boolean isKMSSeparated = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isKMSSeparated = checkIfKMSRolesAreSeparated(vmEsURL, mustFilter);
                if (!isKMSSeparated) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, ruleParam.get(PacmanRuleConstants.DESCRIPTION));
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, ruleParam.get(PacmanRuleConstants.VIOLATION_REASON));
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========SeparationOfDutiesForKMSRule ended with an annotation {} : =========", annotation);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("success::SeparationOfDutiesForKMSRule");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkIfKMSRolesAreSeparated(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("inside checkIfKMSRolesAreSeparated of SeparationOfDutiesForKMSRule");
        boolean cloudKMSAdmin=false;
        boolean encryptDecryptKMS=false;
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        if (hitsJsonArray.size() > 0) {
            JsonObject iamUser = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
            JsonArray roles = iamUser.getAsJsonObject().get(PacmanRuleConstants.ROLES).getAsJsonArray();
            for (JsonElement role : roles) {
                if (role.getAsString().equalsIgnoreCase("roles/cloudkms.admin")) {
                    cloudKMSAdmin = true;
                }
                if (role.getAsString().equalsIgnoreCase("roles/cloudkms.cryptoKeyEncrypter") || role.getAsString().equalsIgnoreCase("roles/cloudkms.cryptoKeyEncrypterDecrypter") || role.getAsString().equalsIgnoreCase("roles/cloudkms.cryptoKeyDecrypter")) {
                    encryptDecryptKMS = true;
                }

            }
        }
            if(cloudKMSAdmin&&encryptDecryptKMS) {
                return false;
            }
            return true;
    }

    @Override
    public String getHelpText() {
        return "Enforce Separation of Duties Is Enforced While Assigning KMS Related Roles to Users";
    }
}
