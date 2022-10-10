package com.tmobile.cloud.gcprules.cloudsql;


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

@PacmanRule(key = "disable-enable-database-flags-for-cloudsql-server", desc = "checks if Google cloud sql server instance database flag is disabled or enabled", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class DisableOrEnableDBFlagsRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(DisableOrEnableDBFlagsRule.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {

        logger.debug("========CloudSQL to check sql server Contained Auth flag Rule is started=========");
        Annotation annotation = null;

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String vmEsURL = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String dbFlagName=ruleParam.get(PacmanRuleConstants.DBFLAGNAME);
        String dbFlagValue=ruleParam.get(PacmanRuleConstants.DBFLAGVALUE);
        String description=ruleParam.get(PacmanRuleConstants.DESCRIPTION);
        String dataBaseType=ruleParam.get(PacmanRuleConstants.DB_TYPE);
        String violation=ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        logger.info("db flag Name,{}, {}",violation,description);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, vmEsURL))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(vmEsURL)) {
            vmEsURL = vmEsURL + "/"+dataBaseType+"/_search";

        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        boolean isDBFlagValueAsExpected= false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isDBFlagValueAsExpected = verifyDBflagEnabledOrDisabled(vmEsURL, mustFilter,dbFlagName,dbFlagValue);
                if (!isDBFlagValueAsExpected) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, description);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, violation);
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========cloud sql with contained Database  flag Rule  ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("========cloud sql with Database flag Rule Ended ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);


    }
    private boolean verifyDBflagEnabledOrDisabled(String vmEsURL, Map<String, Object> mustFilter,String dbFlagName,String dbFlagValue) throws Exception {

        logger.debug("========verifyDBflagEnabledOrDisabled started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            JsonObject dbinstances = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {} and size(){}", dbinstances,hitsJsonArray.size());
            JsonObject settings = dbinstances.getAsJsonObject()
                    .get(PacmanRuleConstants.SETTINGS).getAsJsonObject();
            


            if(settings!=null ){
                JsonArray databaseFlagsList=   settings.get(PacmanRuleConstants.DBFLAGS).getAsJsonArray();
                if(databaseFlagsList.size()>0){
                    for (JsonElement flag: databaseFlagsList) {
                        boolean flagName=flag.getAsJsonObject().get(PacmanRuleConstants.NAME).getAsString().equals(dbFlagName);
                        boolean value=flag.getAsJsonObject().get(PacmanRuleConstants.VALUE).getAsString().equals(dbFlagValue);

                        if( flagName && value ) {
                            validationResult = true;
                            break;
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
        return "check  DB  flag disabled or enabled for cloud sql SERVER Instance";
    }
}
