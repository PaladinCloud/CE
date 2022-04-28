package com.tmobile.cloud.awsrules.securitycenter;

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

@PacmanRule(key = "check-for-ddos-protection-flag-on-azure-virtual-network", desc = "Azure policy for checking the DDOS protection on public virtual networks", severity = PacmanSdkConstants.SEV_HIGH, category = PacmanSdkConstants.SECURITY)
public class AzureVirtualNetworkDdosProtectionRule extends BaseRule {

    private static final Logger logger = LoggerFactory.getLogger(AzureVirtualNetworkDdosProtectionRule.class);
    //private static final String SAMPLE_RESULT=  "{\n  \"took\": 0,\n  \"timed_out\": false,\n  \"_shards\": {\n    \"total\": 3,\n    \"successful\": 3,\n    \"failed\": 0\n  },\n  \"hits\": {\n    \"total\": 2,\n    \"max_score\": 1,\n    \"hits\": [\n      {\n        \"_index\": \"azure_vnet\",\n        \"_type\": \"vnet\",\n        \"_id\": \"subscriptions/17c68d9d-c216-4e06-80ae-22c110ca4cfb/resourceGroups/myTest/providers/Microsoft.Network/virtualNetworks/myVnet\",\n        \"_score\": 1,\n        \"_source\": {\n          \"discoverydate\": \"2022-04-28 08:00:00+0000\",\n          \"_cloudType\": \"Azure\",\n          \"subscription\": \"17c68d9d-c216-4e06-80ae-22c110ca4cfb\",\n          \"region\": \"eastus\",\n          \"subscriptionName\": \"Free Trial\",\n          \"resourceGroupName\": \"myTest\",\n          \"id\": \"subscriptions/17c68d9d-c216-4e06-80ae-22c110ca4cfb/resourceGroups/myTest/providers/Microsoft.Network/virtualNetworks/myVnet\",\n          \"ddosProtectionPlanId\": null,\n          \"hashCode\": 1252357563,\n          \"key\": \"ccb0a4ce-e5e2-4d20-b5be-eeae804b3022\",\n          \"name\": \"myVnet\",\n          \"addressSpaces\": [\n            \"192.168.0.0/16\"\n          ],\n          \"dnsServerIPs\": [],\n          \"subnets\": null,\n          \"tags\": {},\n          \"ddosProtectionEnabled\": true,\n          \"vmProtectionEnabled\": false,\n          \"_resourceid\": \"subscriptions/17c68d9d-c216-4e06-80ae-22c110ca4cfb/resourceGroups/myTest/providers/Microsoft.Network/virtualNetworks/myVnet\",\n          \"_docid\": \"subscriptions/17c68d9d-c216-4e06-80ae-22c110ca4cfb/resourceGroups/myTest/providers/Microsoft.Network/virtualNetworks/myVnet\",\n          \"_entity\": \"true\",\n          \"_entitytype\": \"vnet\",\n          \"firstdiscoveredon\": \"2022-04-28 08:00:00+0000\",\n          \"latest\": true,\n          \"_loaddate\": \"2022-04-28 10:02:00+0000\"\n        }\n      },\n      {\n        \"_index\": \"azure_vnet\",\n        \"_type\": \"vnet\",\n        \"_id\": \"subscriptions/17c68d9d-c216-4e06-80ae-22c110ca4cfb/resourceGroups/myTest2/providers/Microsoft.Network/virtualNetworks/myVnet\",\n        \"_score\": 1,\n        \"_source\": {\n          \"discoverydate\": \"2022-04-28 08:00:00+0000\",\n          \"_cloudType\": \"Azure\",\n          \"subscription\": \"17c68d9d-c216-4e06-80ae-22c110ca4cfb\",\n          \"region\": \"centralus\",\n          \"subscriptionName\": \"Free Trial\",\n          \"resourceGroupName\": \"myTest2\",\n          \"id\": \"subscriptions/17c68d9d-c216-4e06-80ae-22c110ca4cfb/resourceGroups/myTest2/providers/Microsoft.Network/virtualNetworks/myVnet\",\n          \"ddosProtectionPlanId\": null,\n          \"hashCode\": 2113784549,\n          \"key\": \"c4b5e889-b5da-4f6c-ae0c-5c90d29d796a\",\n          \"name\": \"myVnet\",\n          \"addressSpaces\": [\n            \"192.168.0.0/16\"\n          ],\n          \"dnsServerIPs\": [],\n          \"subnets\": null,\n          \"tags\": {},\n          \"ddosProtectionEnabled\": true,\n          \"vmProtectionEnabled\": false,\n          \"_resourceid\": \"subscriptions/17c68d9d-c216-4e06-80ae-22c110ca4cfb/resourceGroups/myTest2/providers/Microsoft.Network/virtualNetworks/myVnet\",\n          \"_docid\": \"subscriptions/17c68d9d-c216-4e06-80ae-22c110ca4cfb/resourceGroups/myTest2/providers/Microsoft.Network/virtualNetworks/myVnet\",\n          \"_entity\": \"true\",\n          \"_entitytype\": \"vnet\",\n          \"firstdiscoveredon\": \"2022-04-28 08:00:00+0000\",\n          \"latest\": true,\n          \"_loaddate\": \"2022-04-28 10:02:00+0000\"\n        }\n      }\n    ]\n  }\n}";

    @Override
    public RuleResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.info("Executing Azure DDOS protection rule for public virtual networks.");

        String severity = ruleParam.get(PacmanRuleConstants.SEVERITY);
        String category = ruleParam.get(PacmanRuleConstants.CATEGORY);


        if (!PacmanUtils.doesAllHaveValue(severity, category)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        String esUrl = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        String url = CommonUtils.getEnvVariableValue(PacmanSdkConstants.ES_URI_ENV_VAR_NAME);
        if (!StringUtils.isNullOrEmpty(url)) {
            esUrl = url + "/azure_vnet/_search";
        }

        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);

        boolean isValid = false;
        if (!StringUtils.isNullOrEmpty(resourceId)) {

            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanUtils.convertAttributetoKeyword(PacmanRuleConstants.RESOURCE_ID), resourceId);
            mustFilter.put(PacmanRuleConstants.LATEST, true);
            try {
                isValid = validateDdosProtectionEnabled(esUrl, mustFilter);
            } catch (Exception e) {
                logger.error("unable to determine", e);
                throw new RuleExecutionFailedExeption("unable to determine" + e);
            }

            if (!isValid) {
                List<LinkedHashMap<String, Object>> issueList = new ArrayList<>();
                LinkedHashMap<String, Object> issue = new LinkedHashMap<>();
                Annotation annotation = null;
                annotation = Annotation.buildAnnotation(ruleParam, Annotation.Type.ISSUE);
                annotation.put(PacmanSdkConstants.DESCRIPTION, "Azure Public virtual network have DDoS protection disabled");
                annotation.put(PacmanRuleConstants.SEVERITY, severity);
                annotation.put(PacmanRuleConstants.CATEGORY, category);
                issue.put(PacmanRuleConstants.VIOLATION_REASON, ruleParam.get(PacmanRuleConstants.RULE_ID) + " Violation Found!");
                issueList.add(issue);
                annotation.put(PacmanRuleConstants.ISSUE_DETAILS, issueList.toString());
                logger.debug("AzureVirtualNetworkDdosProtectionRule completed with FAILURE isValid flag {} : ", isValid);
                return new RuleResult(PacmanSdkConstants.STATUS_FAILURE, PacmanRuleConstants.FAILURE_MESSAGE, annotation);
            }
        }
        logger.debug("AzureVirtualNetworkDdosProtectionRule completed with SUCCESS. isValid flag: {}", isValid);
        return new RuleResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);
    }

    private boolean validateDdosProtectionEnabled(String esUrl, Map<String, Object> mustFilter) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esUrl, mustFilter);
        boolean validationResult = false;
        JsonParser parser = new JsonParser();
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter, new HashMap<>(),
                HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        //JsonObject resultJson= (JsonObject) parser.parse(SAMPLE_RESULT);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson.toString());

        if (resultJson != null && resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = (JsonObject) parser.parse(hitsString);
            JsonArray hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
            if (hitsJsonArray.size() > 0) {
                Object item =  hitsJsonArray.get(0);
                JsonObject jsonDataItem = (JsonObject) ((JsonObject) hitsJsonArray.get(0)).get(PacmanRuleConstants.SOURCE);
                logger.debug("Validating the data item: {}", jsonDataItem.toString());

                if ((null != jsonDataItem.get(PacmanRuleConstants.DDSOS_PROTECTION_FLAG))
                        && (!jsonDataItem.get(PacmanRuleConstants.DDSOS_PROTECTION_FLAG).isJsonNull())) {
                    JsonElement jsonValue = jsonDataItem.get(PacmanRuleConstants.DDSOS_PROTECTION_FLAG);
                    if (jsonValue != null && jsonValue.getAsBoolean()) {
                        logger.debug("DDOS protection flag is enabled for the resource");
                        validationResult = true;
                    }

                } else {
                    logger.debug("DDOS protection is not enabled for resource.");
                    validationResult = false;
                }

            } else {
                logger.info("Resource data not found!! Skipping this validation");
                validationResult = true;
            }
        } else {
            logger.info("Resource data not found!! Skipping this validation");
            validationResult = true;
        }

        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "This rule will check if the DDOS protection is enabled on azure public virtual network.";
    }

    public static void main(String[] args) {
        AzureVirtualNetworkDdosProtectionRule demo=new AzureVirtualNetworkDdosProtectionRule();
        try {
            boolean result=demo.validateDdosProtectionEnabled("",null);
            System.out.println("Result= "+result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
