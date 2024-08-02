package com.paladincloud.common;

import com.paladincloud.common.assets.AssetCounts;
import com.paladincloud.common.assets.AssetCountsHelper;
import com.paladincloud.common.assets.AssetGroupStatsCollector;
import com.paladincloud.common.assets.AssetGroups;
import com.paladincloud.common.assets.AssetRepository;
import com.paladincloud.common.assets.Assets;
import com.paladincloud.common.assets.DataSourceHelper;
import com.paladincloud.common.assets.ElasticAssetRepository;
import com.paladincloud.common.auth.AuthHelper;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.aws.S3Helper;
import com.paladincloud.common.aws.SQSHelper;
import com.paladincloud.common.config.AssetTypes;
import com.paladincloud.common.mapper.MapperRepository;
import com.paladincloud.common.mapper.S3MapperRepository;
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
    SQSHelper provideSQS() {
        return new SQSHelper();
    }

    @Singleton
    @Provides
    AssetTypes provideAssetTypes(ElasticSearchHelper elasticSearch, DatabaseHelper database,
        AssetGroups assetGroups) {
        return new AssetTypes(elasticSearch, database, assetGroups);
    }

    @Singleton
    @Provides
    Assets provideAssets(AssetRepository assetRepository, AssetTypes assetTypes, MapperRepository mapperRepository, DatabaseHelper databaseHelper) {
        return new Assets(assetRepository, assetTypes, mapperRepository, databaseHelper);
    }

    @Singleton
    @Provides
    AssetGroups provideAssetGroups(ElasticSearchHelper elasticSearch, DatabaseHelper database, AuthHelper authHelper) {
        return new AssetGroups(elasticSearch, database);
    }

    @Singleton
    @Provides
    AuthHelper provideAuthHelper() {
        return new AuthHelper();
    }

    @Singleton
    @Provides
    AssetCounts provideAssetCounts(DatabaseHelper databaseHelper, AssetGroups assetGroups, AssetCountsHelper assetCountsHelper) {
        return new AssetCounts(databaseHelper, assetGroups, assetCountsHelper);
    }

    @Singleton
    @Provides
    AssetGroupStatsCollector provideAssetGroupStatsCollector(ElasticSearchHelper elasticSearch, AssetCountsHelper assetCountsHelper) {
        return new AssetGroupStatsCollector(elasticSearch, assetCountsHelper);
    }

    @Singleton
    @Provides
    DataSourceHelper provideDataSourceHelper(ElasticSearchHelper elasticSearch, DatabaseHelper database) {
        return new DataSourceHelper(elasticSearch, database);
    }

    @Singleton
    @Provides
    AssetCountsHelper provideAssetCountsHelper(DatabaseHelper database, AuthHelper authHelper) {
        return new AssetCountsHelper(authHelper, database);
    }

    @Singleton
    @Provides
    MapperRepository provideMapperRepository(S3Helper s3Helper) {
        return new S3MapperRepository(s3Helper);
    }

    @Singleton
    @Provides
    AssetRepository provideAssetRepository(ElasticSearchHelper elasticSearch) {
        return new ElasticAssetRepository(elasticSearch);
    }
}
