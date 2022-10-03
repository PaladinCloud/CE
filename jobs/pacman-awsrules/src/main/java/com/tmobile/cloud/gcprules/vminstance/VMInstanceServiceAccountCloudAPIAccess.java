package com.tmobile.cloud.gcprules.vminstance;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

@PacmanRule(key = "check-for-vminstance-serviceaccount-cloud-access", desc = "Deny usage of service accounts with full cloud API access for VM instances", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class VMInstanceServiceAccountCloudAPIAccess extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(VMInstanceServiceAccountCloudAPIAccess.class);
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========VMInstanceServiceAccountCloudAPIAccess started=========");
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info("{},{},{}",severity,category,vmEsURL);
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_vminstance/_search";
        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        boolean doesVMServiceAccountHaveFullCloudApiAccess = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                doesVMServiceAccountHaveFullCloudApiAccess = verifyVmServiceAccountHaveFullCloudApiAccess(vmEsURL, mustFilter);
                if (doesVMServiceAccountHaveFullCloudApiAccess) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Enforce the principle of least privilege to prevent privilege escalation, ensure that compute engine instances are not configured to use default service account with the cloud api access scope set to \"Allow full acess to all cloud Apis");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);

                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "VM instance service account has cloud api access");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========VMInstanceServiceAccountCloudAPIAccess ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("========VMInstanceServiceAccountCloudAPIAccess ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean verifyVmServiceAccountHaveFullCloudApiAccess(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========verifyVmServiceAccountHaveFullCloudApiAccess started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            JsonObject vmInstanceObject = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
            String name = vmInstanceObject.getAsJsonObject()
                    .get(PacmanRuleConstants.GCP_NAME).getAsString();
            if(name.startsWith("gke"))
            {
                return false;
            }
            logger.debug("Validating the data item: {}", vmInstanceObject);
            String projectNumber = vmInstanceObject.getAsJsonObject()
                    .get(PacmanRuleConstants.GCP_PROJECT_NUMBER).getAsString();
            JsonArray emailList = vmInstanceObject.getAsJsonObject()
                    .get(PacmanRuleConstants.GCP_EMAIL_LIST).getAsJsonArray();
            JsonArray scopesList = vmInstanceObject.getAsJsonObject()
                    .get(PacmanRuleConstants.GCP_SCOPES_LIST).getAsJsonArray();
            String defaultSAEmail=projectNumber.concat("-compute@developer.gserviceaccount.com");
            String defaultSAScope="https://www.googleapis.com/auth/cloud-platform";
            if (emailList.contains(new JsonParser().parse(defaultSAEmail))) {
                for(JsonElement scope:scopesList) {
                    if (scope.getAsString().equals(defaultSAScope)) {
                        validationResult = true;
                    }
                }
            } else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }

        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;

    }

    @Override
    public String getHelpText() {
        return "Deny usage of service accounts with full cloud API access for VM instances";
    }
}
