package com.tmobile.pacman.commons.autofix.manager;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.FirewallsSettings;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyManagementServiceSettings;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.AutoFixManagerFactory;
import com.tmobile.pacman.commons.config.ConfigUtil;
import com.tmobile.pacman.dto.IssueException;
import com.tmobile.pacman.service.ExceptionManager;
import com.tmobile.pacman.service.ExceptionManagerImpl;
import com.tmobile.pacman.util.CloudUtils;
import com.tmobile.pacman.util.CommonUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.*;

public class GcpAutofixManager implements IAutofixManger {

    public static final String VPCFIREWALL = "vpcfirewall";
    public static final String KMSKEY = "kmskey";

    @Override
    public Map<String, Object> getClientMap(String targetTypeAlias, Map<String, String> annotation, String autoFixRole) {

        Map<String, Object> clientMap = new HashMap<>();
        String baseAccount=System.getProperty("base.account");
        String roleName=System.getProperty("s3.role");
        String baseRegion = System.getProperty("base.region");
        String credentialPrefix = System.getProperty("secret.manager.path");

        switch (targetTypeAlias) {
            case VPCFIREWALL:
                try {
                    GoogleCredentials googleCredentials = CloudUtils.getGcpCredentials(baseAccount, baseRegion, roleName, credentialPrefix, annotation.get("accountid").toString());
                    FirewallsSettings firewallsSettings = FirewallsSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
                            .build();
                    FirewallsClient firewallClient = FirewallsClient.create(firewallsSettings);
                    clientMap.put("client", firewallClient);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case KMSKEY:
                try {
                    GoogleCredentials googleCredentials = CloudUtils.getGcpCredentials(baseAccount, baseRegion, roleName, credentialPrefix, annotation.get("accountid").toString());
                    KeyManagementServiceSettings keyManagementServiceSettings = KeyManagementServiceSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials))
                            .build();
                    KeyManagementServiceClient kmsKeyServiceClient = KeyManagementServiceClient.create(keyManagementServiceSettings);
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
                ruleParam.get(PacmanSdkConstants.POLICY_ID), ruleParam.get(PacmanSdkConstants.TARGET_TYPE));
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
