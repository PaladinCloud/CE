package com.tmobile.pacman.commons.autofix.manager;

import com.amazonaws.auth.BasicSessionCredentials;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.AutoFixManagerFactory;
import com.tmobile.pacman.commons.config.ConfigUtil;
import com.tmobile.pacman.commons.dao.RDSDBManager;
import com.tmobile.pacman.config.ConfigManager;
import com.tmobile.pacman.dto.IssueException;
import com.tmobile.pacman.service.ExceptionManager;
import com.tmobile.pacman.service.ExceptionManagerImpl;
import com.tmobile.pacman.util.CloudUtils;
import com.tmobile.pacman.util.CommonUtils;


import java.util.*;

public class AzureAutofixManager implements IAutofixManger{

    RDSDBManager rdsdbManager;


    @Override
    public Map<String, Object> getClientMap(String targetTypeAlias, Map<String, String> annotation, String autoFixRole) {

        Map<String, Object> clientMap = null;
        String subscriptionId = annotation.get(PacmanRuleConstants.AZURE_SUBSCRIPTION);
        String baseAccount = ConfigManager.getConfigurationsMap().get("base.account").toString();
        String region = ConfigManager.getConfigurationsMap().get("base.region").toString();
        String roleName = ConfigManager.getConfigurationsMap().get("s3.role").toString();
        String credentialPrefix=ConfigManager.getConfigurationsMap().get("secret.manager.path").toString();
        List<Map<String, String>> queryResults = RDSDBManager.executeQuery("SELECT tenant FROM cf_AzureTenantSubscription WHERE subscription='"+subscriptionId+"'");
        String tenantId = queryResults.get(0).get("tenant");
        BasicSessionCredentials credentials = CloudUtils.getCredentials(baseAccount, roleName);
        Map<String, String> creds = CloudUtils.decodeCredetials(tenantId,credentials,region,credentialPrefix,roleName);
        String clientId = creds.get("clientId");
        String secret = creds.get("secretId");
        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(clientId,
                    tenantId, secret, AzureEnvironment.AZURE);
        Azure azureClient = Azure.authenticate(applicationTokenCredentials).withSubscription(subscriptionId);
        clientMap = new HashMap<>();
        clientMap.put("client", azureClient);
        return clientMap;
    }

    public void initializeConfigs(){
        try {
            ConfigUtil.setConfigProperties(System.getenv(PacmanSdkConstants.CONFIG_CREDENTIALS),"azure-discovery");
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
            ConfigUtil.setConfigProperties(System.getenv(PacmanSdkConstants.CONFIG_CREDENTIALS),"azure-discovery");
            if( !(params==null || params.isEmpty())){
                params.forEach((k,v) -> System.setProperty(k, v));
            }
        } catch (Exception e) {
            logger.error("Error fetching config", e);
        }
        Properties props = System.getProperties();

        Map<String, String> ruleParam = CommonUtils.createParamMap(args[0]);
        ExceptionManager exceptionManager = new ExceptionManagerImpl();
        Map<String, List<IssueException>> excemptedResourcesForRule = exceptionManager.getStickyExceptions(
                ruleParam.get(PacmanSdkConstants.POLICY_ID), ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
        Map<String, IssueException> individuallyExcemptedIssues = exceptionManager
                .getIndividualExceptions(ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
        IAutofixManger autoFixManager = AutoFixManagerFactory.getAutofixManager("azure");
        autoFixManager.performAutoFixs(ruleParam, excemptedResourcesForRule, individuallyExcemptedIssues);
    }
}
