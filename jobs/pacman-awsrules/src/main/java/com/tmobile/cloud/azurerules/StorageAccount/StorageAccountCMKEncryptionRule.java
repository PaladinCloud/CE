package com.tmobile.cloud.azurerules.StorageAccount;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
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

import java.util.*;

@PacmanRule(key = "check-customer-managed-keys-encryption-enabled-for-storage-account", desc = "Azure policy for checking azure storage accounts are encrypted using customer managed keys", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class StorageAccountCMKEncryptionRule extends BaseRule {

    private static final Logger logger = LoggerFactory
            .getLogger(StorageAccountCMKEncryptionRule.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing Customer Managed Keys For Storage Account Encryption Rule for azure virtual machines");

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (!StringUtils.isNullOrEmpty(esUrl)) {
            esUrl = esUrl + "/azure_storageaccount/_search";
        }

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        boolean isValid = false;
        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = checkIsCustomerManagedKeysEncryptionEnabled(esUrl, mustFilter);
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
                        "Azure storage accounts are not encrypted using customer managed keys");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug("checkIsCustomerManagedKeysEncryptionEnabled completed with FAILURE isValid flag {} : ",
                        isValid);
                return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        }
        logger.debug("checkIsCustomerManagedKeysEncryptionEnabled completed with SUCCESS. isValid flag: {}", isValid);
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkIsCustomerManagedKeysEncryptionEnabled(String esUrl, Map<String, Object> mustFilter)
            throws Exception {
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
                JsonObject sourceJsonObject = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);
                if (sourceJsonObject != null
                        && sourceJsonObject.get(PacmanRuleConstants.CUSTOMER_MANAGED_KEY) != null) {
                    String customerManagedKey = sourceJsonObject.get(PacmanRuleConstants.CUSTOMER_MANAGED_KEY)
                            .getAsString();
                    logger.debug("Validating the data item: {}", customerManagedKey);
                    if (customerManagedKey.isEmpty()) {
                        validationResult = false;
                        logger.debug(
                                "Selected Microsoft Azure storage account is not encrypted using customer managed keys- Validation Result: "
                                        + validationResult);
                    }
                } else {
                    logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
                }
            } else {
                logger.debug(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }
        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "This rule will check if the encryption for selected Microsoft Azure storage accounts is encrypted using customer managed keys";
    }

}
