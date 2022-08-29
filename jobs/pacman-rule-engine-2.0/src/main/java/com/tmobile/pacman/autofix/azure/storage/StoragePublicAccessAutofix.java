package com.tmobile.pacman.autofix.azure.storage;

import com.amazonaws.util.StringUtils;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.BaseFix;
import com.tmobile.pacman.commons.autofix.FixResult;
import com.tmobile.pacman.commons.autofix.PacmanFix;
import com.tmobile.pacman.dto.AutoFixTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@PacmanFix(key = "storage-account-public-access-auto-fix", desc = "Public access on the storage account will be removed")
public class StoragePublicAccessAutofix extends BaseFix {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoragePublicAccessAutofix.class);
    public static final String RESOURCEID = "_resourceid";
    public static final String ACCOUNTID = "accountid";
    public static final String REGION = "region";
    public static final String NAME = "name";
    public static final String NO_DATA = "No Data";

    @Override
    public FixResult executeFix(Map<String, String> issue, Map<String, Object> clientMap, Map<String, String> ruleParams) {
        LOGGER.info("Executing auto fix public access on storage account");
        Azure azure = (Azure) clientMap.get("client");
        String resourceId = issue.get(PacmanSdkConstants.RESOURCE_ID);
        StorageAccount sqlServer = azure.storageAccounts().getById(resourceId);
        if (sqlServer != null) {
            LOGGER.info("Found the matching server instance violating the unrestricted access policy");
            sqlServer.update().disableBlobPublicAccess().apply();
            LOGGER.info("Removed the public access on server :{}", sqlServer.name());
        }
        return new FixResult(PacmanSdkConstants.STATUS_SUCCESS_CODE, "The public access to sql database for resource " + resourceId + " is now revoked");

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
        transactionParams.put(NAME, !StringUtils.isNullOrEmpty(annotation.get(NAME))
                ? annotation.get(NAME) : NO_DATA);
        return new AutoFixTransaction(null, transactionParams);
    }


}
