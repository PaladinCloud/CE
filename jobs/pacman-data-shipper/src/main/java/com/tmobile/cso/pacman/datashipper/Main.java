package com.tmobile.cso.pacman.datashipper;

import com.tmobile.cso.pacman.datashipper.dto.DatasourceData;
import com.tmobile.cso.pacman.datashipper.entity.*;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.ErrorManageUtil;
import com.tmobile.pacman.commons.jobs.PacmanJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.amazonaws.services.stepfunctions.builder.internal.PropertyNames.ERROR;
import static com.tmobile.pacman.commons.utils.Constants.*;

/**
 * The Class Main.
 */
@PacmanJob(methodToexecute = "shipData", jobName = "data-shipper", desc = "Job to load data from s3 to OpenSearch", priority = 5)
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        Arrays.stream(args).forEach(obj -> {
            String[] paramArray = obj.split(":");
            params.put(paramArray[0], paramArray[1]);
        });

        shipData(params);
        System.exit(0);
    }

    /**
     * Ship data.
     *
     * @param params the params
     * @return
     */
    public static Map<String, Object> shipData(Map<String, String> params) {
        String jobName = System.getProperty("jobName");
        List<Map<String, String>> errorList = new ArrayList<>();
        try {
            MainUtil.setup(params);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Exception in setting up Job ");
            errorMap.put(ERROR_TYPE, WARN);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
            return ErrorManageUtil.formErrorCode(jobName, errorList);
        }
        String ds = params.get("datasource");
        ESManager.configureIndexAndTypes(ds, errorList);
        errorList.addAll(new EntityManager().uploadEntityData(ds));
        ExternalPolicies.getInstance().uploadPolicyDefinition(ds);
        try {
            DatasourceData datasourceData = DatasourceDataFetcher.getInstance().fetchDatasourceData(ds);
            if (datasourceData != null) {
                List<String> accountIds = datasourceData.getAccountIds();
                List<String> assetGroups = datasourceData.getAssetGroups();

                if (assetGroups != null && !assetGroups.isEmpty()) {
                    AssetGroupStatsCollector assetGroupStatsCollector = new AssetGroupStatsCollector();
                    errorList.addAll(assetGroupStatsCollector.collectAssetGroupStats(datasourceData));
                }

                if (accountIds != null && !accountIds.isEmpty()) {
                    IssueCountManager issueCountManager = new IssueCountManager();
                    errorList.addAll(issueCountManager.populateViolationsCount(ds, accountIds));

                    AssetsCountManager assetsCountManager = new AssetsCountManager();
                    errorList.addAll(assetsCountManager.populateAssetCount(ds, accountIds));
                }
            } else {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Unexpected error while fetching accountIds and assetGroups, " +
                        "DatasourceData is null");
                errorMap.put(ERROR_TYPE, ERROR);
                errorList.add(errorMap);
                LOGGER.error("Datasource data is null");
            }
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Exception in updating stats");
            errorMap.put(ERROR_TYPE, ERROR);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
            LOGGER.error("Error while updating stats", e);
        }

        Map<String, Object> status = ErrorManageUtil.formErrorCode(jobName, errorList);
        LOGGER.info("Job Return Status {} ", status);

        return status;
    }
}
