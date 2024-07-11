package com.paladincloud.common.assets;

import com.paladincloud.common.aws.DatabaseHelper;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AssetsCounts {

    private final DatabaseHelper database;
    private final AssetGroups assetGroupsInstance;

    @Inject
    public AssetsCounts(DatabaseHelper database, AssetGroups assetGroupsInstance) {
        this.database = database;
        this.assetGroupsInstance = assetGroupsInstance;
    }

    public void populate(String platform, List<String> accountIds) throws Exception {
        for (String accountId : accountIds) {
            int assetCount;
            if (platform.equals("azure")) {
                var subscriptions = getSubscriptionsForTenant(accountId);
                int summedAssetCount = 0;
                for (var subscription : subscriptions) {
                    var count = assetGroupsInstance.fetchAccountAssetCount(platform, subscription);
                    summedAssetCount += count;
                    database.executeUpdate(
                        STR."UPDATE cf_AzureTenantSubscription SET assets=\{count} WHERE subscription ='\{subscription}'");
                }
                assetCount = summedAssetCount;
            } else {
                assetCount = assetGroupsInstance.fetchAccountAssetCount(platform, accountId);
            }

            database.executeUpdate(STR."UPDATE cf_Accounts SET assets=\{assetCount} WHERE accountId='\{accountId}'");
        }
    }

    private List<String> getSubscriptionsForTenant(String accountId) {
        var subscriptions = database.executeQuery(
            STR."SELECT subscription FROM cf_AzureTenantSubscription WHERE tenant='\{accountId}'");
        return subscriptions.stream().map(row -> row.get("subscription")).toList();
    }
}
