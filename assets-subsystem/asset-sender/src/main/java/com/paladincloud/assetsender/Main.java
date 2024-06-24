package com.paladincloud.assetsender;

import com.paladincloud.common.DaggerServerComponent;

public class Main {

    public static void main(String[] args) {
        var componentResolver = DaggerServerComponent.create();
        System.exit(componentResolver.buildAssetSenderJob().run(args));
    }
}
