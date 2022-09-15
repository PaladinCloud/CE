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

@PacmanRule(key = "enable-soft-delete-for-blob-storage-account", desc = "Ensure that Soft Delete feature is enabled for all your Microsoft Azure Storage blob objects", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class EnableSoftDeleteBlobStorageRule extends BaseRule {
    private static final Logger logger = LoggerFactory.getLogger(EnableSoftDeleteBlobStorageRule.class);
    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing public access rule for storage account");

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);

        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);

        if (!StringUtils.isNullOrEmpty(esUrl)) {
            esUrl = esUrl + "/azure_blobservice/_search";
        }

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        boolean isSoftDeleteEnabled = false;
        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isSoftDeleteEnabled = checkSoftDeleteEnabled(esUrl, mustFilter);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

            if (!isSoftDeleteEnabled) {
                List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                Annotation annotation = null;
                annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION,
                        " Soft Delete feature is  disabled enabled for all your Microsoft Azure Storage blob objects");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                annotation.put(PacmanRuleConstants.NAME,resourceAttributes.get(PacmanRuleConstants.NAME));
                issue.put(PacmanRuleConstants.VIOLATION_REASON,
                        ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug("Storage account soft delete feature disabled !!!.");
                return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE,
                        annotation);
            }
        }
        logger.debug("Storage account soft delete feature enabled !!!.");
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);    }
    private boolean checkSoftDeleteEnabled(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);
        boolean validationResult = false;
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(),
                HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson.toString());

        if (resultJson != null && resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = JsonParser.parseString(hitsString).getAsJsonObject();
            JsonArray hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
            if (hitsJsonArray.size() > 0) {
                JsonObject sourceJsonObject = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);
                if (sourceJsonObject != null && sourceJsonObject.get(PacmanRuleConstants.PROPERTIESMAP) != null) {
                    JsonObject propertiesMap = sourceJsonObject.get(PacmanRuleConstants.PROPERTIESMAP)
                            .getAsJsonObject();
                    logger.debug("Validating the data item: {}", propertiesMap);
                    if (propertiesMap!=null &&propertiesMap.get(PacmanRuleConstants.DELETE_RETENTION_POLICY)!=null) {

                        if(propertiesMap.get(PacmanRuleConstants.DELETE_RETENTION_POLICY).getAsJsonObject().get(PacmanRuleConstants.ENABLED).getAsBoolean()==true){
                            validationResult=true;
                        }
                    }
                    else{
                        logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);

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
        return "Enable Soft Delete for Blob Storage Account";
    }
}
