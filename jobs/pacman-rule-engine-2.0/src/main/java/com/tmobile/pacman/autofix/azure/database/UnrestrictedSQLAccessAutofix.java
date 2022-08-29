package com.tmobile.pacman.autofix.azure.database;

import com.amazonaws.util.StringUtils;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.BaseFix;
import com.tmobile.pacman.commons.autofix.FixResult;
import com.tmobile.pacman.commons.autofix.PacmanFix;
import com.tmobile.pacman.dto.AutoFixTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PacmanFix(key = "unrestricted-sql-access-auto-fix", desc = "Network security group rules providing public access on port will be removed")
public class UnrestrictedSQLAccessAutofix extends BaseFix {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnrestrictedSQLAccessAutofix.class);
    public static final String RESOURCEID = "_resourceid";
    public static final String ACCOUNTID = "accountid";
    public static final String REGION = "region";
    public static final String NAME = "name";
    public static final String NO_DATA = "No Data";

    @Override
    public FixResult executeFix(Map<String, String> issue, Map<String, Object> clientMap, Map<String, String> ruleParams) {
        LOGGER.info("Executing auto fix unrestricted sql server access");
        Azure azure = (Azure) clientMap.get("client");

        PagedList<SqlServer> sqlServers = azure.sqlServers().list();
        String resourceId = issue.get(RESOURCEID);
        SqlServer sqlServerInstance=null;
        for (SqlServer sqlServer : sqlServers) {
            List<SqlDatabase> sqlDatabases = azure.sqlServers().databases().listBySqlServer(sqlServer);
            for (SqlDatabase sqlDatabase : sqlDatabases) {
                String id = sqlDatabase.databaseId();
                String sqlDbId = id.startsWith("/") ? id.substring(1) : id;
                if (StringUtils.compare(resourceId, sqlDbId) == 0) {
                    sqlServerInstance = sqlServer;
                    break;
                }
            }
        }
        if(sqlServerInstance!=null){
            LOGGER.info("Found the matching server instance violating the unrestricted access policy");
            for (SqlFirewallRule sqlFirewallRule : sqlServerInstance.firewallRules().list()) {
                if(StringUtils.compare(sqlFirewallRule.startIPAddress(),"0.0.0.0")==0){
                    LOGGER.info("Found the firewall rule allowing unrestricted access to sql database. Rule name: {}",sqlFirewallRule.name());
                    sqlFirewallRule.delete();
                    LOGGER.info("Firewall rule:{} allowing unrestricted access to sql database is now deleted",sqlFirewallRule.name());
                }
            }
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
