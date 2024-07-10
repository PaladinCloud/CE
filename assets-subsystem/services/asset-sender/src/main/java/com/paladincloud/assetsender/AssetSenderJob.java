package com.paladincloud.assetsender;

import com.paladincloud.common.ProcessingDoneMessage;
import com.paladincloud.common.assets.Assets;
import com.paladincloud.common.aws.SQSHelper;
import com.paladincloud.common.config.AssetTypes;
import com.paladincloud.common.config.ConfigConstants;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.jobs.JobExecutor;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AssetSenderJob extends JobExecutor {

    public static final String DATA_SOURCE = "data-source";
    public static final String S3_PATH = "s3-path";
    public static final String TENANT_ID = "tenant-id";
    private static final Logger LOGGER = LogManager.getLogger(AssetSenderJob.class);

    private final AssetTypes assetTypes;
    private final Assets assets;
    private final SQSHelper sqsHelper;

    @Inject
    AssetSenderJob(AssetTypes assetTypes, Assets assets, SQSHelper sqsHelper) {
        this.assetTypes = assetTypes;
        this.assets = assets;
        this.sqsHelper = sqsHelper;
    }


    @Override
    protected void execute() {
        var dataSource = params.get(DATA_SOURCE);
        var tenantId = params.get(TENANT_ID);
        LOGGER.info("Processing assets; bucket={} datasource={} path={} tenant={}",
            ConfigService.get(ConfigConstants.S3.BUCKET_NAME), dataSource, params.get(S3_PATH),
            tenantId);
        ConfigService.setProperties("batch.",
            Collections.singletonMap("s3.data", params.get(S3_PATH)));
        assetTypes.setupIndexAndTypes(dataSource);
        assets.upload(dataSource);

        if ("true".equalsIgnoreCase(ConfigService.get(ConfigConstants.Dev.OMIT_DONE_EVENT))) {
            LOGGER.warn("Omitting done event");
        } else {
            sqsHelper.sendMessage(params.get(JobExecutor.ASSET_SHIPPER_DONE_SQS_URL),
                new ProcessingDoneMessage(STR."\{dataSource}-asset-shipper", tenantId, dataSource,
                    null),
                UUID.randomUUID().toString());
        }
    }

    @Override
    protected List<String> getRequiredFields() {
        return List.of(DATA_SOURCE, S3_PATH, TENANT_ID);
    }
}
