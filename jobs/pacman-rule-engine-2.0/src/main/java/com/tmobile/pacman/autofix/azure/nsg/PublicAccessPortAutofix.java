package com.tmobile.pacman.autofix.azure.nsg;

import com.amazonaws.util.StringUtils;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.BaseFix;
import com.tmobile.pacman.commons.autofix.FixResult;
import com.tmobile.pacman.commons.autofix.PacmanFix;
import com.tmobile.pacman.dto.AutoFixTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PacmanFix(key = "public-access-nsg-auto-fix", desc = "Network security group rules providing public access on port will be removed")
public class PublicAccessPortAutofix extends BaseFix {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicAccessPortAutofix.class);
    public static final String RESOURCEID = "_resourceid";
    public static final String ACCOUNTID = "accountid";
    public static final String REGION = "region";
    public static final String NAME = "name";
    public static final String NO_DATA = "No Data";

    @Override
    public FixResult executeFix(Map<String, String> issue, Map<String, Object> clientMap, Map<String, String> ruleParams) {
        LOGGER.info("Auto fix execution");
        Azure azure = (Azure) clientMap.get("client");

        PagedList<NetworkSecurityGroup> securityGroups = azure.networkSecurityGroups().list();

        String portValue = ruleParams.get("port");
        String protocol = ruleParams.get("protocol");
        String resourceId = issue.get(RESOURCEID);
        for (NetworkSecurityGroup securityGroup : securityGroups) {
            securityGroup.resourceGroupName();

            String id = securityGroup.id();
            if (id != null && id.startsWith("/")) {
                id = id.substring(1);
            }
            if (resourceId.equalsIgnoreCase(id)) {
                //Fix the security group rule
                for (Map.Entry<String, NetworkSecurityRule> entry : securityGroup.securityRules().entrySet()) {
                    NetworkSecurityRule securityRule = entry.getValue();
                    String nsgProtocol = securityRule.protocol().toString();
                    boolean protocolCheck = nsgProtocol.equalsIgnoreCase(protocol) || nsgProtocol.equalsIgnoreCase(PacmanRuleConstants.PORT_ANY);
                    boolean destinationPortCheck = securityRule.destinationPortRanges().contains(portValue) || securityRule.destinationPortRange().contains(portValue) || portValue.equalsIgnoreCase(PacmanRuleConstants.PORT_ANY);
                    String securityRuleName = null;
                    if (securityRule.direction().toString().equals("Inbound") && protocolCheck && destinationPortCheck) {
                        List<String> sourceAddressPrefixes = securityRule.sourceAddressPrefixes();
                        if (sourceAddressPrefixes != null && !sourceAddressPrefixes.isEmpty()) {
                            for (String sourcePrefix : sourceAddressPrefixes) {
                                if (sourcePrefix.equalsIgnoreCase(PacmanRuleConstants.ANY) || sourcePrefix.equalsIgnoreCase(PacmanRuleConstants.PORT_ANY) || sourcePrefix.equalsIgnoreCase(PacmanRuleConstants.INTERNET)) {
                                    securityRuleName = entry.getValue().name();
                                }
                            }
                        } else {
                            String sourcePrefix = securityRule.sourceAddressPrefix();
                            if (sourcePrefix.equalsIgnoreCase(PacmanRuleConstants.ANY) || sourcePrefix.equalsIgnoreCase(PacmanRuleConstants.PORT_ANY) || sourcePrefix.equalsIgnoreCase(PacmanRuleConstants.INTERNET)) {
                                securityRuleName = entry.getValue().name();
                            }
                        }
                    }
                    if (securityRuleName != null) {
                        securityGroup.update().withoutRule(securityRuleName).apply();
                        LOGGER.info("Security rule :{} is deleted as it is violating the azure policy.",securityRuleName);
                    }
                }
            }
        }
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
        transactionParams.put(PacmanSdkConstants.SUBSCRIPTION,
                !StringUtils.isNullOrEmpty(annotation.get(PacmanSdkConstants.SUBSCRIPTION)) ?
                        annotation.get(PacmanSdkConstants.SUBSCRIPTION) : NO_DATA);
        transactionParams.put(REGION, !StringUtils.isNullOrEmpty(annotation.get(REGION))
                ? annotation.get(REGION) : NO_DATA);
        transactionParams.put(NAME, !StringUtils.isNullOrEmpty(annotation.get(NAME))
                ? annotation.get(NAME) : NO_DATA);
        return new AutoFixTransaction(null, transactionParams);
    }


}
