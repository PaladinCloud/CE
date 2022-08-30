package com.tmobile.cloud.gcprules.cloudsql;

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

@PacmanRule(key = "check-if-allowlist-all-public-ip-for-db", desc = "Check if Whitelisting of all Public IP Addresses Cloud SQL Database Instances ", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class DenyWhitelistAllPublicIpRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(DenyWhitelistAllPublicIpRule.class);
    private static final String VALUE = "value";

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("Executing public ip rule for cloud sql instances");
        Annotation annotation = null;
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String description = ruleParam.get(PacmanRuleConstants.DESCRIPTION);
        String violationReason = ruleParam.get(PacmanRuleConstants.VIOLATION_REASON);
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String esEndpoint = ruleParam.get(PacmanRuleConstants.ES_SG_RULES_URL);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category, esUrl))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        if (!StringUtils.isNullOrEmpty(esUrl)) {
            esUrl = esUrl + esEndpoint;
        }
        logger.debug("ES search url for gcp cloud sql:  {}", esUrl);
        boolean authorizedNetworkFlag = false;
        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put(PacmanSdkConstants.RULE_ID, ruleParam.get(PacmanSdkConstants.RULE_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                authorizedNetworkFlag = validateAuthorizedNetworkValueForSQLInstance(esUrl, mustFilter);
                if (authorizedNetworkFlag) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, description);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                    logger.debug("Cloud sql whitelist all public ip ended with failure. Annotation {} :", annotation);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("Cloud sql whitelist all public ip rule ended with success.");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean validateAuthorizedNetworkValueForSQLInstance(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.debug("Validating public ip for sql instances");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(esUrl, mustFilter);
        if (hitsJsonArray.size() > 0) {
            JsonObject sourceData = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
            logger.debug("Data retrieved from ES: {}", sourceData);
            JsonObject settings = sourceData.getAsJsonObject()
                    .get(PacmanRuleConstants.SETTINGS).getAsJsonObject();
            if(settings !=null){
                JsonObject ipConfiguration=settings.getAsJsonObject().get(PacmanRuleConstants.IP_CONFIG).getAsJsonObject();
                if(ipConfiguration!=null){
                    JsonArray authorizedNetworks=ipConfiguration.getAsJsonObject().get(PacmanRuleConstants.AUTHORIZED_NETWORK).getAsJsonArray();
                    if(!authorizedNetworks.isEmpty()){
                        for (int i = 0; i < authorizedNetworks.size(); i++) {
                            JsonObject authorizedNetwork=  ((JsonObject) authorizedNetworks.get(i));
                            String type = authorizedNetwork.get(VALUE).getAsString();
                            logger.debug("The value of authorizedNetwork :{}",type);
                            if (type.equalsIgnoreCase("0.0.0.0/0") || type.equalsIgnoreCase("::/0") ) return true;
                        }
                    }else {
                        logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
                    }
                }else {
                    logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
                }
            }else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }
        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return false;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if  if your Cloud SQL database instances are configured to allow access to anyone on the Internet,";
    }
}
