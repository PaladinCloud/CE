package com.tmobile.cloud.gcprules.vminstance;


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
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

@PacmanRule(key = "deny-usage-of-default-service-account", desc = "Deny usage of default service accounts", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class DenyUsageOfDefaultServiceAccount extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(DenyUsageOfDefaultServiceAccount.class);
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("======== DenyUsageOfDefaultServiceAccount Rule started=========");
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
            vmEsURL = vmEsURL + "/gcp_vminstance/_search";
        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        boolean isDefaultServiceAccountConfigured = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isDefaultServiceAccountConfigured = checkDefaultServiceAccountConfigured(vmEsURL, mustFilter);
                if (isDefaultServiceAccountConfigured) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "ensure that your Google Compute Engine instances are not configured to use the default service account");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);

                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "Google Compute Engine instances are  configured to use the default service account");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========DefaultServiceAccountUsageRule ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("========DefaultServiceAccountUsageRule ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkDefaultServiceAccountConfigured(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========DefaultServiceAccountUsageRule started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            JsonObject vmInstanceObject = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", vmInstanceObject.toString());
            JsonArray serviceAccountsList = vmInstanceObject.getAsJsonObject()
                    .get(PacmanRuleConstants.SERVICE_ACCOUNTS_LIST).getAsJsonArray();
                String projectNumber=null;
                if(vmInstanceObject.get(PacmanRuleConstants.PROJECT_NUMBER) !=null){
                    projectNumber=vmInstanceObject.get(PacmanRuleConstants.PROJECT_NUMBER).getAsString();

                }
            if (serviceAccountsList.size() > 0) {
                for (JsonElement serviceAccount :serviceAccountsList) {
                    logger.debug("serviceAccountsList SIZE: {}", serviceAccountsList.size());

                   if(projectNumber!=null &&serviceAccount.getAsJsonObject().get(PacmanRuleConstants.EMAIL)!=null){
                      String email=serviceAccount.getAsJsonObject().get(PacmanRuleConstants.EMAIL).getAsString();
                       logger.info("email {} ",email);

                       String defaultEmail=null;
                       if(projectNumber!=null) {
                           defaultEmail=  projectNumber + PacmanRuleConstants.EMAIL_PATTERN;
                       }
                       logger.info("defaultEmail {} ",defaultEmail);

                       if(defaultEmail != null && defaultEmail.equals(email)){
                          validationResult=true;
                      }

                   }

                }
                }

             else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }

        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Deny usage of default service accounts";
    }
}
