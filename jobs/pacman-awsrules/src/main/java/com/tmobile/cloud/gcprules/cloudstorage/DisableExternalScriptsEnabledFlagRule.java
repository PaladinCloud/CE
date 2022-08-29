package com.tmobile.cloud.gcprules.cloudstorage;

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

@PacmanRule(key = "check-if-external-script-Enabled-flag-for-sql-server-disabled", desc = "check if the 'external scripts enabled' database flag is turned off for your Google Cloud SQL Server database instances in order to disable the execution of scripts with certain remote language extensions", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class DisableExternalScriptsEnabledFlagRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(DisableExternalScriptsEnabledFlagRule.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========DisableExternalScriptsEnabledFlagRule started=========");
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String dbFlagName=ruleParam.get(PacmanRuleConstants.DBFLAGNAME);

        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        //EsUrl will be changed for sqlserver db instance.
        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/gcp_cloudsql_sqlserver/_search";
        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        boolean isValid = false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isValid = isExternalScriptFlagDisable(vmEsURL, mustFilter,dbFlagName);
                if (isValid) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "External scripts enabled flag is ON for sql server database");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "External scripts enabled flag is ON for sql server database");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========DisableExternalScriptsEnabledFlagRule ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("External scripts enabled flag is OFF for sql server database");
        logger.debug("========DisableExternalScriptsEnabledFlagRule ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean isExternalScriptFlagDisable(String vmEsURL, Map<String, Object> mustFilter,String dbFlagName) throws Exception {
        logger.debug("======== isExternalScriptFlagDisable started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            JsonObject dbinstances = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {} and size(){}", dbinstances,hitsJsonArray.size());
            JsonObject settings = dbinstances.getAsJsonObject()
                    .get(PacmanRuleConstants.SETTINGS).getAsJsonObject();

            if(settings!=null ){
                JsonArray databaseFlagsList= settings.get(PacmanRuleConstants.DBFLAGS).getAsJsonArray();
                if(databaseFlagsList.size()>0){
                    for (JsonElement flag: databaseFlagsList) {
                        boolean flagName=flag.getAsJsonObject().get(PacmanRuleConstants.NAME).getAsString().equals(dbFlagName);
                        boolean value=flag.getAsJsonObject().get(PacmanRuleConstants.VALUE).getAsString().equals("on");

                        if( flagName && value ) {
                            validationResult = true;
                        }
                    }
                }
            }
            else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }

            }  else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "To determine if your Google Cloud SQL server database instances have external scripts enabled flag is disabled.";
    }

}
