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
@PacmanRule(key = "check-if-sql-instance-owner-changing-flag-disabled", desc = "checks if Google sql instance owner changing flag is disabled", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class DisableDBOwnerRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(DisableDBOwnerRule.class);
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("========CloudStorage to check Db Owner flag rule is started=========");
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
            vmEsURL = vmEsURL + "/gcp_cloudsql/_search";

        }
        logger.debug("========vmEsURL URL after concatenation param {}  =========", vmEsURL);

        boolean isdbOwnerChangingflagEnabled= false;

        MDC.put("executionId", ruleParam.get("executionId"));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);

            try {
                isdbOwnerChangingflagEnabled = verifyDbOwnerChangingflagEnabled(vmEsURL, mustFilter);
                if (isdbOwnerChangingflagEnabled) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "The cross db ownership chaining configuration flag allows you to control cross-database ownership chaining at the SQL Server database level or to allow cross-database ownership chaining for all SQL Server databases. Enabling \"cross db ownership chaining\" flag is not recommended unless all of the databases hosted by the SQL Server need to participate in cross-database ownership chaining and you are fully aware of the security implications of this configuration setting");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "\"cross db ownership chaining\" flag is enabled for your Google Cloud SQL Server database instances");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========CloudStorageWithPublicAccessRule ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("========CloudStorageWithPublicAccessRule ended=========");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean verifyDbOwnerChangingflagEnabled(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("========verifyIfverifyDbOwnerChangingflagEnabled started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        boolean validationResult = false;
        if (hitsJsonArray.size() > 0) {
            JsonObject dbinstances = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);

            logger.debug("Validating the data item: {}", dbinstances);
            JsonObject settings = dbinstances.getAsJsonObject()
                    .get(PacmanRuleConstants.SETTINGS).getAsJsonObject();
            if(settings!=null){
             JsonArray databaseFlagsList=   settings.get(PacmanRuleConstants.DBFLAGS).getAsJsonArray();
             if(databaseFlagsList.size()>0){
                 for (JsonElement flag: databaseFlagsList) {
                     boolean flagName=flag.getAsJsonObject().get(PacmanRuleConstants.NAME).getAsString().equals(PacmanRuleConstants.DB_PROPERTY_OWNER_CHANGING_FLAG);
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

        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "this rule checks cross DB owner changing Flag disabled  for Google SQL Instance";
    }
}