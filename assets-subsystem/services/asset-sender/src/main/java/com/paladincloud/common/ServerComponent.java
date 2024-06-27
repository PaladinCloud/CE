package com.paladincloud.common;

import com.paladincloud.assetsender.AssetSenderJob;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ServerComponent {

    AssetSenderJob buildAssetSenderJob();
}
