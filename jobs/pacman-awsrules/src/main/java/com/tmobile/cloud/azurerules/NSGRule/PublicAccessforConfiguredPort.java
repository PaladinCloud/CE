package com.tmobile.cloud.azurerules.NSGRule;

import java.util.Map;

import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.PacmanRule;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import okhttp3.Protocol;

import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.utils.CommonUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;

@PacmanRule(key = "check-for-azure-nsg-rule", desc = "Deny unrestricted Access to Azure Resources", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class PublicAccessforConfiguredPort extends BaseRule {
    private static final Logger logger = LoggerFactory
            .getLogger(PublicAccessforConfiguredPort.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing Azure Security rule");
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String[] port = ruleParam.get(PacmanRuleConstants.PORT).split(",");
        String protocol = ruleParam.get(PacmanRuleConstants.PROTOCOL);
        logger.info("check Public Access for the   port: {} ", ((Object[]) port));
        logger.info("check Public Access for the Protocol : {} ", protocol);

        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String url = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (!StringUtils.isNullOrEmpty(url)) {
            esUrl = url + "/azure_nsg/_search";
        }
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        boolean isValid = false;
        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = validatePostgresqlServerAccess(esUrl, mustFilter, port, protocol);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

            if (!isValid) {
                List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                Annotation annotation = null;
                annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION,
                        "Azure   has unrestricted Access");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug(
                        "has restricted  Access for the  port :{} Rule completed with FAILURE isValid flag {} : ",
                        port, isValid);
                return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        }

        logger.debug("has unrestricted for port :{} Rule completed with Success isValid flag {}",
                port, isValid);
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean validatePostgresqlServerAccess(String esUrl, Map<String, Object> mustFilter, String[] validatePort,
            String validateProtocol) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);
        boolean validationResult = true;
        JsonParser parser = new JsonParser();
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(),
                HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson.toString());

        if (resultJson != null && resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = (JsonObject) parser.parse(hitsString);
            JsonArray hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
            if (hitsJsonArray.size() > 0) {
                JsonObject jsonDataItem = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);
                logger.debug("Validating the data item: {}", jsonDataItem.toString());
                JsonArray inBoundarySecurityJsonArray = jsonDataItem.getAsJsonObject()
                        .get(PacmanRuleConstants.AZURE_INBOUNDARYSECURITYRULES).getAsJsonArray();
                if (inBoundarySecurityJsonArray.size() > 0) {
                    for (int i = 0; i < inBoundarySecurityJsonArray.size(); i++) {
                        JsonObject nBoundarySecurityDataItem = ((JsonObject) inBoundarySecurityJsonArray
                                .get(i));
                        JsonArray sourceAddressPrefixes = nBoundarySecurityDataItem.getAsJsonObject()
                                .get(PacmanRuleConstants.SECURITY_RULE_SOURCEADDRESSPREFIXES).getAsJsonArray();
                        String protocol = nBoundarySecurityDataItem.getAsJsonObject()
                                .get(PacmanRuleConstants.PROTOCOL).getAsString();
                        if (sourceAddressPrefixes != null && (protocol.equalsIgnoreCase(validateProtocol)||protocol.equalsIgnoreCase(PacmanRuleConstants.PORT_ANY)
                                && checkDestinationPort(nBoundarySecurityDataItem.getAsJsonObject()
                                        .get(PacmanRuleConstants.DESTINATIONPORTRANGES).getAsJsonArray(),
                                        validatePort,validateProtocol))) {
                            for (int srcAdsIndex = 0; srcAdsIndex < sourceAddressPrefixes.size(); srcAdsIndex++) {
                                if (sourceAddressPrefixes.get(srcAdsIndex).getAsString()
                                        .equals(PacmanRuleConstants.PORT_ANY)
                                        || sourceAddressPrefixes.get(srcAdsIndex).getAsString()
                                                .equals(PacmanRuleConstants.ANY)
                                        || sourceAddressPrefixes.get(srcAdsIndex).getAsString()
                                                .equals(PacmanRuleConstants.INTERNET)) {
                                    logger.info("Port: {} has unrestricted Access", ((Object[]) validatePort));
                                    validationResult = false;
                                    break;

                                }
                            }

                        } else {
                            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);

                        }

                    }
                    if (validationResult == true) {
                        logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
                    }

                } else {
                    logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
                    validationResult = false;
                }

            } else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }
        }
        return validationResult;
    }

    private boolean checkDestinationPort(JsonArray destinationPorts, String[] validatePorts,String protocol) {
        logger.info("checkDestinationPort");
        if(protocol.equals(PacmanRuleConstants.UDP))
            return true;
        for (int i = 0; i < destinationPorts.size(); i++) {
            if (ArrayUtils.contains(validatePorts, destinationPorts.get(i).toString())
                    || ArrayUtils.contains(validatePorts, PacmanRuleConstants.PORT_ANY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getHelpText() {
        return "This rule will check Postgre sql server has restricted Access ";
    }

}
