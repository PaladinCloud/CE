package com.tmobile.cloud.azurerules.Subscription;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
@PacmanRule(key="check-for-custom-owner-roles", desc = "Subscription ownership should not include permission to create custom owner roles.", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class RemoveCustomOwnerRoles extends BaseRule {

    private static final Logger logger = LoggerFactory.getLogger(RemoveCustomOwnerRoles.class);

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing RemoveCustomOwnerRole ");
        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);
        String successMSG = ruleParam.get(PacmanRuleConstants.SUCCESS);
        String failureMsg = ruleParam.get(PacmanRuleConstants.FAILURE);
        if (Boolean.FALSE.equals(PacmanUtils.doesAllHaveValue(severity, category))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String url = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (!StringUtils.isNullOrEmpty(url)) {
            esUrl = url + "/azure_subscription/_search";
        }
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        logger.info("resourceId: {} ", resourceId);
        boolean isValid = false;

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.AZURE_SUBSCRIPTION), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = checkCustomOwnerRole(esUrl, mustFilter);
                if (isValid) {
                    List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                    LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                    Annotation annotation = null;
                    annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                    annotation.put(PacmanSdkConstants.DESCRIPTION, failureMsg);
                    annotation.put(PacmanRuleConstants.SEVERITY, severity);
                    annotation.put(PacmanRuleConstants.CATEGORY, category);
                    issue.put(PacmanRuleConstants.VIOLATION_REASON, ruleParam.get(PacmanRuleConstants.RULE_ID) + failureMsg + " Violation Found!");
                    issueList.add(issue);
                    annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                    logger.debug(failureMsg);
                    return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
                }
            } catch (Exception e) {
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }
        }

        logger.debug(successMSG);
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean checkCustomOwnerRole(String esUrl, Map<String, Object> mustFilter) throws Exception {
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
                JsonObject jsonDataItem = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                        .get(PacmanRuleConstants.SOURCE);
                logger.debug("Validating the data item: {}", jsonDataItem.toString());

                String subscriptionId=jsonDataItem.getAsJsonObject().get("subscriptionId").getAsString();
                logger.debug("Subscription id {}",subscriptionId);

                JsonArray roleDefinitionList=jsonDataItem.getAsJsonObject().get("roleDefinitionList").getAsJsonArray();
                logger.debug("roleDefinitionList size{}",roleDefinitionList);

                if(!roleDefinitionList.isEmpty()){
                    for(int i=0;i<roleDefinitionList.size();i++){
                        JsonObject roleDefinitionObj=((JsonObject) roleDefinitionList.get(i));

                        boolean actionFlag=false;
                        JsonArray actions=roleDefinitionObj.getAsJsonObject().get("actions").getAsJsonArray();

                        for(JsonElement action:actions){
                            String actionValue=action.getAsString();
                            logger.debug("actionValue {}",actionValue);

                            if(actionValue.equalsIgnoreCase("*")) {
                                actionFlag = true;
                                break;
                            }
                        }

                        boolean assignableScopesFlag=false;
                        JsonArray assignableScopes=roleDefinitionObj.getAsJsonObject().get("assignableScopes").getAsJsonArray();

                        for(JsonElement assignableScope:assignableScopes){
                            String assignableScopeValue=assignableScope.getAsString();
                            logger.debug("assignableScopes {}",assignableScopeValue);

                            if(assignableScopeValue.equalsIgnoreCase("/")) {
                                assignableScopesFlag = true;
                                break;
                            }
                            else if (assignableScopeValue.equalsIgnoreCase("/subscriptions/"+subscriptionId)) {
                                assignableScopesFlag=true;
                                break;
                            }
                        }

                        if(actionFlag && assignableScopesFlag){
                            validationResult=true;
                            break;
                        }
                    }
                }
                else {
                    logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
                }
            } else {
                logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            }
        } else {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
        }
        return validationResult;
    }


    @Override
    public String getHelpText() {
        return "This rule checks if there are no custom subscription owner roles available in your Azure account in order to adhere to cloud security best practices and implement the principle of least privilege ";
    }

}
