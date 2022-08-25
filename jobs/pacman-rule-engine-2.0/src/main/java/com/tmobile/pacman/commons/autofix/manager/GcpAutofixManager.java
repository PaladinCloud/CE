package com.tmobile.pacman.commons.autofix.manager;

import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.AutoFixManagerFactory;
import com.tmobile.pacman.commons.config.ConfigUtil;
import com.tmobile.pacman.dto.IssueException;
import com.tmobile.pacman.service.ExceptionManager;
import com.tmobile.pacman.service.ExceptionManagerImpl;
import com.tmobile.pacman.util.CommonUtils;

import java.io.IOException;
import java.util.*;

public class GcpAutofixManager implements IAutofixManger {

    public static final String VPCFIREWALL = "vpcfirewall";
    public static final String KMSKEY = "kmskey";

    @Override
    public Map<String, Object> getClientMap(String targetTypeAlias, Map<String, String> annotation, String autoFixRole) {

        Map<String, Object> clientMap = new HashMap<>();
        ;
        switch (targetTypeAlias) {
            case VPCFIREWALL:
                try {
                    FirewallsClient firewallClient = gcpCredentialsProvider.getFirewallsClient();
                    clientMap.put("client", firewallClient);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case KMSKEY:
                try {
                    KeyManagementServiceClient kmsKeyServiceClient = gcpCredentialsProvider.getKmsKeyServiceClient();
                    clientMap.put("client", kmsKeyServiceClient);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
        return clientMap;
    }


    public static void main(String[] args) throws Exception {
        CommonUtils.getPropValue(PacmanSdkConstants.ORPHAN_RESOURCE_OWNER_EMAIL);
        Map<String, String> params = new HashMap<>();
        Arrays.asList(args).stream().forEach(obj -> {
            String[] keyValue = obj.split("[:]");
            params.put(keyValue[0], keyValue[1]);
        });
        try {
            ConfigUtil.setConfigProperties(System.getenv(PacmanSdkConstants.CONFIG_CREDENTIALS),"gcp-discovery");
            if (!(params == null || params.isEmpty())) {
                params.forEach((k, v) -> System.setProperty(k, v));
            }
            //System.getenv().forEach((k,v) -> System.setProperty(k, v));
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
        IAutofixManger autoFixManager = AutoFixManagerFactory.getAutofixManager("gcp");
        autoFixManager.performAutoFixs(ruleParam, excemptedResourcesForRule, individuallyExcemptedIssues);

    }

    @Override
    public void initializeConfigs() {
        try {
            ConfigUtil.setConfigProperties(System.getenv(PacmanSdkConstants.CONFIG_CREDENTIALS),"gcp-discovery");
        } catch (Exception e) {
            logger.error("Error fetching config", e);
        }
    }
}
