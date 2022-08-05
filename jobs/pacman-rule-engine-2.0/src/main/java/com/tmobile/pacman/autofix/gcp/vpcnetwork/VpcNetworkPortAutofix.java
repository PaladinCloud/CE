package com.tmobile.pacman.autofix.gcp.vpcnetwork;

import com.amazonaws.util.StringUtils;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.Operation;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.common.exception.AutoFixException;
import com.tmobile.pacman.commons.autofix.BaseFix;
import com.tmobile.pacman.commons.autofix.FixResult;
import com.tmobile.pacman.commons.autofix.PacmanFix;
import com.tmobile.pacman.dto.AutoFixTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@PacmanFix(key = "vpc-firewall-port-access-fix", desc = "VPC firewall rules providing public access on port will be removed")
public class VpcNetworkPortAutofix extends BaseFix {

    private static final Logger LOGGER = LoggerFactory.getLogger(VpcNetworkPortAutofix.class);
    public static final String RESOURCEID = "_resourceid";
    public static final String ACCOUNTID = "accountid";
    public static final String REGION = "region";
    public static final String NAME = "name";
    public static final String NO_DATA = "No Data";

    @Override
    public FixResult executeFix(Map<String, String> issue, Map<String, Object> clientMap, Map<String, String> ruleParams) {
        LOGGER.info("Executing autofix for vpc firewall port violation.");
        FirewallsClient firewallsClient = (FirewallsClient) clientMap.get("client");

        String firewallRuleName = issue.get(PacmanRuleConstants.FIREWALL_RULE_NAME);
        String resourceId = issue.get(RESOURCEID);
        String portValue = ruleParams.get(PacmanRuleConstants.PORT);
        String projectId = issue.get(PacmanSdkConstants.PROJECT_NAME);
        Operation response = null;
        try {
            response = firewallsClient.deleteAsync(projectId, firewallRuleName).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Operation response:" + response);
        return new FixResult(PacmanSdkConstants.STATUS_SUCCESS_CODE, "The public access to port " + portValue + " for resource " + resourceId + " is now revoked");
    }

    @Override
    public boolean backupExistingConfigForResource(String resourceId, String resourceType, Map<String, Object> clientMap, Map<String, String> ruleParams, Map<String, String> issue) throws Exception {
        return false;
    }

    @Override
    public AutoFixTransaction addDetailsToTransactionLog(Map<String, String> annotation) {
        LinkedHashMap<String, String> transactionParams = new LinkedHashMap();
        transactionParams.put("resourceId",
                !StringUtils.isNullOrEmpty(annotation.get(RESOURCEID)) ? annotation.get(RESOURCEID) : NO_DATA);
        transactionParams.put(PacmanSdkConstants.PROJECT_NAME,
                !StringUtils.isNullOrEmpty(annotation.get(PacmanSdkConstants.PROJECT_NAME)) ?
                        annotation.get(PacmanSdkConstants.PROJECT_NAME) : NO_DATA);
        transactionParams.put(PacmanRuleConstants.FIREWALL_RULE_NAME,
                !StringUtils.isNullOrEmpty(annotation.get(PacmanRuleConstants.FIREWALL_RULE_NAME)) ?
                        annotation.get(PacmanRuleConstants.FIREWALL_RULE_NAME) : NO_DATA);
        return new AutoFixTransaction(null, transactionParams);
    }


}
