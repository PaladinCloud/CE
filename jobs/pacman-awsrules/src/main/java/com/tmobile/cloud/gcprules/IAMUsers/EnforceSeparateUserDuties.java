package com.tmobile.cloud.gcprules.IAMUsers;

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

@PacmanRule(key = "enforce-Separate-Service-Account-Duties-for-Users",desc = "Ensure That Separation of Duties Is Enforced While Assigning Service Account Related Roles to Users",severity = PacmanRuleConstants.HIGH,category = PacmanSdkConstants.SECURITY)
public class EnforceSeparateUserDuties extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(EnforceSeparateUserDuties.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========EnforceSeparateUserDuties rule started=========");
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
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        boolean isSeparateDutiesAssigned = false;
        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isSeparateDutiesAssigned = checkforSeparateDuties(vmEsURL, mustFilter);
                if (isSeparateDutiesAssigned) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "It is recommended that the principle of 'Separation of Duties' is enforced while assigning service-account related roles to users.");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);

                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "Same Service Account Duties for Users");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========Same Service Account Duties for Users{} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("========Separate Service Account Duties for Users=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }
    private boolean checkforSeparateDuties(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        logger.debug("========checkUserManagedKeysDeleted started========= {}",hitsJsonArray);

        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            JsonObject iamUsers = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", iamUsers.toString());
            JsonArray roles = iamUsers.getAsJsonObject()
                    .get(PacmanRuleConstants.ROLES).getAsJsonArray();
            int duties=0;
            for (JsonElement role:roles){
                logger.info("Roles *** {}",role);
                if(role.getAsString().equalsIgnoreCase("roles/iam.serviceAccountAdmin")|| role.getAsString().equalsIgnoreCase("roles/iam.serviceAccountUser")){
                        duties++;
                }
            }
            if(duties>=2){
                validationResult=true;
            }


        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
       }

        return validationResult;
    }
    @Override
    public String getHelpText() {
        return "Enforce Separate Service Account Duties for Users";
    }
}
