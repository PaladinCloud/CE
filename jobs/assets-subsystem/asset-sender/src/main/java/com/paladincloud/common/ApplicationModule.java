package com.paladincloud.common;

import com.paladincloud.common.assets.Assets;
import com.paladincloud.common.aws.Database;
import com.paladincloud.common.aws.S3;
import com.paladincloud.common.config.AssetTypes;
import com.paladincloud.common.search.ElasticSearch;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class ApplicationModule {

    @Singleton
    @Provides
    ElasticSearch provideElasticSearch() {
        return new ElasticSearch();
    }

    @Singleton
    @Provides
    Database provideDatabase() {
        return new Database();
    }

    @Singleton
    @Provides
    S3 provideS3() {
        return new S3();
    }

    @Singleton
    @Provides
    AssetTypes provideAssetTypes(ElasticSearch elasticSearch, Database database) {
        return new AssetTypes(elasticSearch, database);
    }

    @Singleton
    @Provides
    Assets provideAssets(ElasticSearch elasticSearch, AssetTypes assetTypes, S3 s3, Database database) {
        return new Assets(elasticSearch, assetTypes, s3, database);
    }
}
