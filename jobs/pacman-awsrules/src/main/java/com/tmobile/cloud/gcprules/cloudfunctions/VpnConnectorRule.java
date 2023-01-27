package com.tmobile.cloud.gcprules.cloudfunctions;

import com.amazonaws.util.StringUtils;
import com.google.gson.JsonObject;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.cloud.constants.PacmanRuleDescriptionConstants;
import com.tmobile.cloud.constants.PacmanRuleViolationReasonConstants;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PacmanPolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

@PacmanPolicy(key = "GCP-Cloud-Function-not-enabled-with-VPC-connector", desc = "GCP Cloud Function not enabled with VPC connector", severity = PacmanSdkConstants.SEV_MEDIUM, category = PacmanSdkConstants.SECURITY)
public class VpnConnectorRule extends BasePolicy {
    private static final Logger logger = LoggerFactory.getLogger(VpnConnectorRule.class);
    @Override
    public PolicyResult execute(Map<String, String> ruleParam, Map<String, String> resourceAttributes) {
        logger.debug("Executing GCP Cloud Function not enabled with VPC connector rule for gcp cloud function");
        String resourceId = ruleParam.get(PacmanRuleConstants.RESOURCE_ID);
        String esUrl = GCPUtils.getEsUrl(ruleParam);
        if (Boolean.FALSE.equals(GCPUtils.validateRuleParam(ruleParam))) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }
        logger.debug("========gcp_gcploadbalancer URL after concatenation param {}  =========", esUrl);
        MDC.put(PacmanSdkConstants.EXECUTION_ID, ruleParam.get("executionId"));
        MDC.put(PacmanSdkConstants.POLICY_ID, ruleParam.get(PacmanSdkConstants.POLICY_ID));

        if (!StringUtils.isNullOrEmpty(resourceId)) {
            logger.debug("========after url");
            try {
                boolean isVpcConnector = checkIfVpcConnectorIsEnabled(esUrl, resourceId);
                if (!isVpcConnector) {
                    return GCPUtils.fetchPolicyResult(ruleParam, PacmanRuleDescriptionConstants.VPC_CONNECTOR,
                            PacmanRuleViolationReasonConstants.VPC_CONNECTOR);
                }
            } catch (Exception exception) {
                throw new RuleExecutionFailedExeption(exception.getMessage());
            }
        }
        logger.debug("======== ended with status true=========");
        return new PolicyResult(PacmanSdkConstants.STATUS_SUCCESS, PacmanRuleConstants.SUCCESS_MESSAGE);

    }

    private boolean checkIfVpcConnectorIsEnabled(String vmEsURL, String resourceId) throws Exception {
        logger.debug("========checkIfVpcConnectorIsEnabled  started=========");
        boolean validationResult = false;
        JsonObject sourceData = GCPUtils.getJsonObjFromSourceData(vmEsURL, resourceId);
        if(sourceData != null){
            String vpcConnector = sourceData.getAsJsonObject().get(PacmanRuleConstants.VPC_CONNECTOR).getAsString();
            if(!StringUtils.isNullOrEmpty(vpcConnector)){
                validationResult = true;
            }
            logger.debug("Validating the data item: {}", sourceData);
        }
        return validationResult;
    }

    @Override
    public String getHelpText() {
        return "GCP Cloud Functions are not configured with a VPC connector.";
    }
}
