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
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.PacmanRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
@PacmanRule(key = "disable-basic-authentication", desc = "Disable Basic Authentication using static passwords", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class DisableBasicAuthenticationRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(DisableBasicAuthenticationRule.class);
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
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
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isKeyEnabled = checkIfBasicAuthIsDisabled(vmEsURL, mustFilter);
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
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }

        logger.debug("======== ended with status true=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkIfBasicAuthIsDisabled(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========checkIfBasicAuthIsDisabled  started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = true;
        if (hitsJsonArray.size() > 0) {
            logger.debug("========checkIfBasicAuthIsDisabled hit array=========");

            JsonObject gkeCluster = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", gkeCluster);

            String username = gkeCluster.getAsJsonPrimitive("username").getAsString();
            String password = gkeCluster.getAsJsonPrimitive("password").getAsString();
            String version=gkeCluster.getAsJsonPrimitive("version").getAsString().substring(0,4);
            if(versionCompare(version,"1.19")<0 &&!username.isEmpty() && !password.isEmpty())
            {
                validationResult=false;
            }

        }

        return validationResult;
    }
    // Method to compare two versions. Returns 1 if v2 is smaller, -1 if v1 is smaller, 0 if equal
    static int versionCompare(String v1, String v2)
    {
        // vnum stores each numeric part of version
        int vnum1 = 0;
        int vnum2 = 0;

        // loop until both String are processed
        for (int i = 0, j = 0; (i < v1.length()
                || j < v2.length());) {
            // Storing numeric part of
            // version 1 in vnum1
            while (i < v1.length()
                    && v1.charAt(i) != '.') {
                vnum1 = vnum1 * 10 + (v1.charAt(i) - '0');
                i++;
            }

            // storing numeric part
            // of version 2 in vnum2
            while (j < v2.length()
                    && v2.charAt(j) != '.') {
                vnum2 = vnum2 * 10 + (v2.charAt(j) - '0');
                j++;
            }

            if (vnum1 > vnum2)
                return 1;
            if (vnum2 > vnum1)
                return -1;

            // if equal, reset variables and
            // go for next numeric part
            vnum1 = vnum2 = 0;
            i++;
            j++;
        }
        return 0;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if basic authentication is disabled";
    }
}
