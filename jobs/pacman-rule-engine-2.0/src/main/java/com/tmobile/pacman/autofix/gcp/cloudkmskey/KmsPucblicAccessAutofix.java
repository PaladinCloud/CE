package com.tmobile.pacman.autofix.gcp.cloudkmskey;

import com.amazonaws.util.StringUtils;
import com.google.cloud.kms.v1.CryptoKey;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.BaseFix;
import com.tmobile.pacman.commons.autofix.FixResult;
import com.tmobile.pacman.commons.autofix.PacmanFix;
import com.tmobile.pacman.dto.AutoFixTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@PacmanFix(key = "kms-public-access-auto-fix", desc = "Public access on cloud KMS key will be removed")
public class KmsPucblicAccessAutofix extends BaseFix {

    private static final Logger logger = LoggerFactory.getLogger(KmsPucblicAccessAutofix.class);
    public static final String RESOURCEID = "_resourceid";
    public static final String ACCOUNTID = "accountid";
    public static final String REGION = "region";
    public static final String NAME = "name";
    public static final String NO_DATA = "No Data";

    @Override
    public FixResult executeFix(Map<String, String> issue, Map<String, Object> clientMap, Map<String, String> ruleParams) {
        logger.info("Executing autofix for public access on cloud KMS keys.");
        KeyManagementServiceClient keyManagementServiceClient = (KeyManagementServiceClient) clientMap.get("client");

        String resourceId = issue.get(RESOURCEID);
        CryptoKey cryptoKey = null;
        try {

            Policy iamPolicy = keyManagementServiceClient.getIamPolicy(resourceId);
            logger.info("Iam policies: {}", iamPolicy);
            if (iamPolicy != null &&  iamPolicy.getBindingsList()!=null && iamPolicy.getBindingsList().isEmpty()) {
                for (Binding binding : iamPolicy.getBindingsList()) {
                    if (binding.getMembersList().contains(PacmanRuleConstants.ALL_USERS)
                            || binding.getMembersList().contains(PacmanRuleConstants.ALL_AUTH_USERS)) {
                        logger.info("AllUsers/AllAuthententicatedUser role is providing public access to key.");
                        binding.getMembersList().remove(PacmanRuleConstants.ALL_USERS);
                        binding.getMembersList().remove(PacmanRuleConstants.ALL_AUTH_USERS);
                        logger.info("Public access to key is now revoked.");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception occurred while executing autofix for public kms key");
        }
        return new FixResult(PacmanSdkConstants.STATUS_SUCCESS_CODE, "The public access to KMS key " + resourceId +  " is now revoked");
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
        return new AutoFixTransaction(null, transactionParams);
    }
}
