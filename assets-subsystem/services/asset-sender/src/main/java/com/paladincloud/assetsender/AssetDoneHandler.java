package com.paladincloud.assetsender;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.paladincloud.common.DaggerServerComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AssetDoneHandler implements RequestHandler<SQSEvent, Integer> {

    private static final Logger LOGGER = LogManager.getLogger(AssetDoneHandler.class);

    @Override
    public Integer handleRequest(SQSEvent event, Context context) {
        var componentResolver = DaggerServerComponent.create();
        for (var message : event.getRecords()) {
            var args = message.getBody().split(" ");
            componentResolver.buildAssetSenderJob().run(args);
        }

        return 0;
    }
}
