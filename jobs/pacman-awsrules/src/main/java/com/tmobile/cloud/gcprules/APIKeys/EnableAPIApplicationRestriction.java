package com.tmobile.cloud.gcprules.APIKeys;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.gcprules.bigquery.DatasetAccesRule;
import com.tmobile.cloud.gcprules.cloudsql.DenyPublicIpForDbInstanceRule;
import com.tmobile.cloud.gcprules.cloudsql.EnforceSSLToCloudSQL;
import com.tmobile.cloud.gcprules.iam.AvoidAssigningServiceRolesRule;
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


@PacmanRule(key = "Check-for-API-Key-Application-Restrictions", desc = "Check for API Key Application Restrictions", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class EnableAPIApplicationRestriction extends BaseRule {

    private static final Logger logger = LoggerFactory.getLogger(EnableAPIApplicationRestriction.class);
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("executing EnableAPIApplicationRestriction....");
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
            vmEsURL = vmEsURL + "/gcp_apikeys/_search";
        }
        logger.debug("========gcp_iamusers URL after concatenation param {}  =========", vmEsURL);

        boolean isnotRoleRestricted = false;

        MDC.put("executionId",ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put("ruleId", ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
       mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isnotRoleRestricted= restrictApplicationAPI(vmEsURL, mustFilter);
                if (isnotRoleRestricted) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();

                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, "Ensure that Google Cloud API key usage is restricted to trusted hosts, HTTP referrers, or applications");
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, "API Key Application is not restricted");
                    issueList.add(issue);
                    annotation.put("issueDetails", issueList.toString());
                    logger.debug("========EnableAPIApplicationRestriction ended with an annotation {} : =========", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }

            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("EnableAPIApplicationRestriction ended with success MSG");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean restrictApplicationAPI(String vmEsURL, Map<String, Object> mustFilter) throws Exception {
        logger.debug("inside restrictApplicationAPI");
        boolean validationResult=false;
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        if (hitsJsonArray.size() > 0) {
            logger.info("hit array size {}",hitsJsonArray.size());
           JsonObject apiKeys = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                   .get(PacmanRuleConstants.SOURCE);

            if (apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject()!=null &&apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().size()>0) {
                logger.info("android Key {} {} {} {} ",apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("androidKeyRestrictions").getAsJsonObject().size(),apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("serverKeyRestrictions").getAsJsonObject().size(),apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("iosKeyRestrictions").getAsJsonObject().size(),apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("browserKeyRestrictions").getAsJsonObject().size());
                if(apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("androidKeyRestrictions").getAsJsonObject().size()==0 &&apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("serverKeyRestrictions").getAsJsonObject().size()==0&& apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("iosKeyRestrictions").getAsJsonObject().size()==0&&apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("browserKeyRestrictions").getAsJsonObject().size()==0){
                        validationResult=true;

                }
                else if(apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("serverKeyRestrictions").getAsJsonObject().size()!=0 ){
                  JsonArray ips=  apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("serverKeyRestrictions").getAsJsonObject().get("allowed_ips").getAsJsonArray();
                    for(JsonElement ip:ips){
                        if( ip.getAsString().equalsIgnoreCase("0.0.0.0")||ip.getAsString().equalsIgnoreCase("0.0.0.0/0")||ip.getAsString().equalsIgnoreCase("::0")){
                            validationResult=true;
                        }

                    }

                } else if (apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("browserKeyRestrictions").getAsJsonObject().size()>0) {
                    JsonArray allowed_referrers=  apiKeys.get(PacmanRuleConstants.RESTRICTIONS).getAsJsonObject().get("browserKeyRestrictions").getAsJsonObject().get("allowed_referrers").getAsJsonArray();
                    for(JsonElement allowed_referrer:allowed_referrers){
                        if( allowed_referrer.getAsString().contains("*")){
                            validationResult=true;
                        }

                    }
                }

            }
            else{
                validationResult=true;
            }


        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "Check for API Key Application Restrictions.";
    }
}
