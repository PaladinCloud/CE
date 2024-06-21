package com.paladincloud.common;

import com.paladincloud.common.assets.Assets;
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
    AssetTypes provideAssetTypes() {
        return new AssetTypes(provideElasticSearch());
    }

    @Singleton
    @Provides
    Assets provideAssets() {
        return new Assets(provideElasticSearch(), provideAssetTypes());
    }
}
