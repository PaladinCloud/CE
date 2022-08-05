package com.tmobile.pacman.commons.autofix.manager;

import com.amazonaws.regions.Regions;
import com.microsoft.azure.management.Azure;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacbot.azure.inventory.config.ConfigUtil;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.AWSService;
import com.tmobile.pacman.commons.autofix.AutoFixManagerFactory;
import com.tmobile.pacman.commons.aws.clients.AWSClientManager;
import com.tmobile.pacman.commons.aws.clients.impl.AWSClientManagerImpl;
import com.tmobile.pacman.commons.exception.UnableToCreateClientException;
import com.tmobile.pacman.dto.IssueException;
import com.tmobile.pacman.service.ExceptionManager;
import com.tmobile.pacman.service.ExceptionManagerImpl;
import com.tmobile.pacman.util.CommonUtils;

import java.util.*;

public class AwsAutofixManager implements IAutofixManger{

    @Override
    public Map<String, Object> getClientMap(String targetType, Map<String, String> annotation, String ruleIdentifyingString) throws Exception {

        StringBuilder roleArn = new StringBuilder();
        Map<String, Object> clientMap = null;
        roleArn.append(PacmanSdkConstants.ROLE_ARN_PREFIX).append(annotation.get(PacmanSdkConstants.ACCOUNT_ID))
                .append(":").append(ruleIdentifyingString);

        AWSClientManager awsClientManager = new AWSClientManagerImpl();
        try {
            clientMap = awsClientManager.getClient(annotation.get(PacmanSdkConstants.ACCOUNT_ID), roleArn.toString(),
                    AWSService.valueOf(targetType.toUpperCase()), Regions.fromName(
                            annotation.get(PacmanSdkConstants.REGION) == null ? Regions.DEFAULT_REGION.getName()
                                    : annotation.get(PacmanSdkConstants.REGION)),
                    ruleIdentifyingString);
        } catch (UnableToCreateClientException e1) {
            logger.error("unable to create client for account {} and region {} and {}" , annotation.get(PacmanSdkConstants.ACCOUNT_ID),annotation.get(PacmanSdkConstants.REGION),e1);
            throw new Exception("unable to create client for account and region");
        }
        return clientMap;
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
        IAutofixManger autoFixManager = AutoFixManagerFactory.getAutofixManager("aws");
        autoFixManager.performAutoFixs(ruleParam, excemptedResourcesForRule, individuallyExcemptedIssues);

    }

    @Override
    public void initializeConfigs() {

    }
}
