package com.paladincloud.assetsender;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.paladincloud.common.DaggerServerComponent;

public class AssetDoneHandler implements RequestHandler<SQSEvent, Integer> {

    @Override
    public Integer handleRequest(SQSEvent event, Context context) {
        var componentResolver = DaggerServerComponent.create();
        for (var message : event.getRecords()) {
            var args = message.getBody().split(" ");
            componentResolver.buildAssetSenderJob().run("AssetShipper", args);
        }

        return 0;
    }
}
