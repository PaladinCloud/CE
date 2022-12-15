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
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

@PacmanPolicy(key = "check-if-ip-public-for-db", desc = "Check if ip is public for sql db instance", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class DenyPublicIpForDbInstanceRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(DenyPublicIpForDbInstanceRule.class);
    private static final String IPADDRESS ="ipAddress" ;
    private static final String TYPE = "type";

    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
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
        boolean publicIpFlag = false;
        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get(PacmanSdkConstants.EXECUTION_ID));
        MDC.put(PacmanSdkConstants.POLICY_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                publicIpFlag = validatePublicIpForSQLInstance(esUrl, mustFilter);
                if (publicIpFlag) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, description);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, violationReason);
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                    logger.debug("Cloud sql public ip ended with failure. Annotation {} :", annotation);
                    return new PolicyResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                            annotation);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("Cloud sql instances public ip rule ended with success.");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean validatePublicIpForSQLInstance(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.debug("Validating public ip for sql instances");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(esUrl, mustFilter);
        if (hitsJsonArray.size() > 0) {
            JsonObject sourceData = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                    .get(PacmanRuleConstants.SOURCE);
            logger.debug("Data retrieved from ES: {}", sourceData);
            JsonArray ipAddress = sourceData.getAsJsonObject().get(IPADDRESS).getAsJsonArray();
            if (!ipAddress.isEmpty()) {
                for (int i = 0; i < ipAddress.size(); i++) {
                    JsonObject ip=  ((JsonObject) ipAddress.get(i));
                    String type = ip.get(TYPE).getAsString();
                    logger.debug("The type of ip :{}",type);
                    if (type.equals("PRIMARY")) return true;
                }
            } else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }

        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return false;
    }

    @Override
    public String getHelpText() {
        return "This rule checks if sql instances have public ips";
    }
}
