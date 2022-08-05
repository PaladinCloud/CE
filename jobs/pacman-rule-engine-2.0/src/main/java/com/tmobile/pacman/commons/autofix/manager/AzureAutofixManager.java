package com.tmobile.pacman.commons.autofix.manager;

import com.microsoft.azure.management.Azure;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacbot.azure.inventory.config.ConfigUtil;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.AutoFixManagerFactory;
import com.tmobile.pacman.dto.IssueException;
import com.tmobile.pacman.service.ExceptionManager;
import com.tmobile.pacman.service.ExceptionManagerImpl;
import com.tmobile.pacman.util.CommonUtils;

import java.util.*;

public class AzureAutofixManager implements IAutofixManger{

    @Override
    public Map<String, Object> getClientMap(String targetTypeAlias, Map<String, String> annotation, String autoFixRole) {

        Map<String, Object> clientMap = null;
        String subscriptionId = annotation.get(PacmanRuleConstants.AZURE_SUBSCRIPTION);

        String tenant = System.getProperty("azure.credentials");
        String tenantStr = tenant.split("tenant:")[1];
        String tenantId = tenantStr.substring(0, tenantStr.indexOf(","));
        Azure azureClient = azureCredentialProvider.authenticate(tenantId, subscriptionId);
        clientMap = new HashMap<>();
        clientMap.put("client", azureClient);
        return clientMap;
    }

    public void initializeConfigs(){
        try {
            ConfigUtil.setConfigProperties(System.getenv(PacmanSdkConstants.CONFIG_CREDENTIALS));
        } catch (Exception e) {
            logger.error("Error fetching config", e);
        }
        Properties props = System.getProperties();
    }

    public static void main(String[] args) throws Exception {
        CommonUtils.getPropValue(PacmanSdkConstants.ORPHAN_RESOURCE_OWNER_EMAIL);
        Map<String,String> params = new HashMap<>();
        Arrays.asList(args).stream().forEach(obj-> {
            String[] keyValue = obj.split("[:]");
            params.put(keyValue[0], keyValue[1]);
        });
        try {
            ConfigUtil.setConfigProperties(System.getenv(PacmanSdkConstants.CONFIG_CREDENTIALS));
            if( !(params==null || params.isEmpty())){
                params.forEach((k,v) -> System.setProperty(k, v));
            }
        } catch (Exception e) {
            logger.error("Error fetching config", e);
            //ErrorManageUtil.uploadError("all", "all", "all", "Error fetching config "+ e.getMessage());
            //return ErrorManageUtil.formErrorCode();
        }
        Properties props = System.getProperties();

        Map<String, String> ruleParam = CommonUtils.createParamMap(args[0]);
        ExceptionManager exceptionManager = new ExceptionManagerImpl();
        Map<String, List<IssueException>> excemptedResourcesForRule = exceptionManager.getStickyExceptions(
                ruleParam.get(PacmanSdkConstants.RULE_ID), ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
        Map<String, IssueException> individuallyExcemptedIssues = exceptionManager
                .getIndividualExceptions(ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
        IAutofixManger autoFixManager = AutoFixManagerFactory.getAutofixManager("azure");
        autoFixManager.performAutoFixs(ruleParam, excemptedResourcesForRule, individuallyExcemptedIssues);

    }
}
