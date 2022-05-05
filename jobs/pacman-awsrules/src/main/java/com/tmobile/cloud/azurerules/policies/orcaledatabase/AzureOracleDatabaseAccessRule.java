package com.tmobile.cloud.azurerules.policies.orcaledatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

import com.tmobile.pacman.commons.rule.BaseRule;
import com.tmobile.pacman.commons.rule.RuleResult;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.rule.PacmanRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.util.StringUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.Annotation;
import com.tmobile.pacman.commons.utils.CommonUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;

@PacmanRule(key = "check-for-unrestricted-oracle-database-acesss", desc = "unrestricted access for oracle database", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class AzureOracleDatabaseAccessRule extends BaseRule {
    private static final Logger logger = LoggerFactory
            .getLogger(AzureOracleDatabaseAccessRule.class);

    private static final String RESOURCE_NOT_Found = "Resource data not found!!Skipping this validation";

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing Azure Unristricted Access rule for Oracle Database.");

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

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
                isValid = validateOracleDatabaseAccess(esUrl, mustFilter);
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
                        "Azure Oracle Database  has restricted Access");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug("Azure Unrestricted Oracle Database Access Rule completed with FAILURE isValid flag {} : ",
                        isValid);
                return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        }

        logger.debug("Azure Unrestricted Oracle Database Access Rule completed with Success isValid flag {}", isValid);
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean validateOracleDatabaseAccess(String esUrl, Map<String, Object> mustFilter) throws Exception {
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
                        if (sourceAddressPrefixes != null && protocol.equalsIgnoreCase("TCP")
                                && checkDestinationPort(nBoundarySecurityDataItem.getAsJsonObject()
                                        .get(PacmanRuleConstants.SECURITY_RULE_SOURCEADDRESSPREFIXES)
                                        .getAsJsonArray())) {
                            for (int srcAdsIndex = 0; srcAdsIndex < sourceAddressPrefixes.size(); srcAdsIndex++) {
                                if (sourceAddressPrefixes.get(srcAdsIndex).getAsString().equals("*")
                                        || sourceAddressPrefixes.get(srcAdsIndex).getAsString().equals("any")
                                        || sourceAddressPrefixes.get(srcAdsIndex).getAsString().equals("internet")) {
                                    logger.info("Oracle Database has unrestricted Access");
                                    validationResult = false;
                                    break;

                                }
                            }

                        } else {
                            logger.info(RESOURCE_NOT_Found);

                        }

                    }
                    if (validationResult == true) {
                        logger.info(" Oracle Database has Restricted Access");
                    }

                } else {
                    logger.info(RESOURCE_NOT_Found);
                    validationResult = false;
                }

            } else {
                logger.info(RESOURCE_NOT_Found);
            }
        }

        return validationResult;

    }

    private boolean checkDestinationPort(JsonArray jsonArray) {

        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i).toString().equals(PacmanRuleConstants.PORT_ANY)
                    || jsonArray.get(i).toString().equals(PacmanRuleConstants.PORT_1521)) {
                return true;
            }
        }

        return false;

    }

    @Override
    public String getHelpText() {
        return "This rule will check Oracle Database has unrestricted Acess ";
    }

    public static void main(String[] args) {
        AzureOracleDatabaseAccessRule demo = new AzureOracleDatabaseAccessRule();
        try {
            boolean result = demo.validateOracleDatabaseAccess("", null);
            System.out.println("Result= " + result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}