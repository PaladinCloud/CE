package com.paladincloud.common.assets;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AssetGroups {

    private static final Logger LOGGER = LogManager.getLogger(AssetGroups.class);

    private AssetGroups() {
    }

    public static void createDefaultGroup(String dataSource) {
        // TODO - create the group
        LOGGER.error("TODO: Update aliases");
        /**
         try {
         String queryForAllResources = "SELECT EXISTS (SELECT 1 FROM " +
         "cf_AssetGroupDetails WHERE groupName = '" + ASSET_GROUP_FOR_ALL_RESOURCES + "') AS row_exists";
         Optional<String> isAllResourceExists = RDSDBManager.executeStringQuery(queryForAllResources)
         .stream().findFirst();
         if (isAllResourceExists.isPresent() && isAllResourceExists.get().equalsIgnoreCase("0")) {
         // Creates ASSET_GROUP_FOR_ALL_RESOURCES if not present
         String aliasQuery = getAliasQueryForDefaultAssetGroup(datasource);
         createDefaultAssetGroup(aliasQuery);
         ESManager.invokeAPI("POST", "_aliases/", aliasQuery);
         LOGGER.info("Created default asset group with group name as {}", ASSET_GROUP_FOR_ALL_RESOURCES);
         }
         } catch (Exception e) {
         LOGGER.error("Unexpected error occurred while creating default asset group", e);
         }
         */
    }

    public static void updateImpactedAliases(List<String> aliases, String dataSource) {
        if (aliases.isEmpty()) {
            return;
        }
        LOGGER.warn("The impacted aliases need to be updated for {}: {}", dataSource, aliases);
    }
}
