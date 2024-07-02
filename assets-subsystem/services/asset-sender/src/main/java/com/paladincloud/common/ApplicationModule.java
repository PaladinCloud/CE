package com.paladincloud.common;

import com.paladincloud.common.assets.AssetGroups;
import com.paladincloud.common.assets.Assets;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.aws.S3Helper;
import com.paladincloud.common.config.AssetTypes;
import com.paladincloud.common.search.ElasticSearchHelper;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class ApplicationModule {

    @Singleton
    @Provides
    ElasticSearchHelper provideElasticSearch() {
        return new ElasticSearchHelper();
    }

    @Singleton
    @Provides
    DatabaseHelper provideDatabase() {
        return new DatabaseHelper();
    }

    @Singleton
    @Provides
    S3Helper provideS3() {
        return new S3Helper();
    }

    @Singleton
    @Provides
    AssetTypes provideAssetTypes(ElasticSearchHelper elasticSearch, DatabaseHelper database, AssetGroups assetGroups) {
        return new AssetTypes(elasticSearch, database, assetGroups);
    }

    @Singleton
    @Provides
    Assets provideAssets(ElasticSearchHelper elasticSearch, AssetTypes assetTypes, S3Helper s3, DatabaseHelper database) {
        return new Assets(elasticSearch, assetTypes, s3, database);
    }

    @Singleton
    @Provides
    AssetGroups provideAssetGroups(ElasticSearchHelper elasticSearch, DatabaseHelper database) {
        return new AssetGroups(elasticSearch, database);
    }
}
