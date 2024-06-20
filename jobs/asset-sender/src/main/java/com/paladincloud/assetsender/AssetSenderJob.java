package com.paladincloud.assetsender;

import com.paladincloud.common.assets.Assets;
import com.paladincloud.common.config.ConfigConstants;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.config.Types;
import com.paladincloud.common.jobs.JobExecutor;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AssetSenderJob extends JobExecutor {

    public static final String DATA_SOURCE = "data-source";
    public static final String S3_PATH = "s3-path";
    public static final String TENANT_ID = "tenant-id";
    private static final Logger LOGGER = LogManager.getLogger(AssetSenderJob.class);

    @Override
    protected int execute() {

        LOGGER.info("Processing assets; bucket={} datasource={} path={} tenant={}",
            ConfigService.get(ConfigConstants.S3.BUCKET_NAME), params.get(DATA_SOURCE),
            params.get(S3_PATH), params.get(TENANT_ID));
        ConfigService.setProperties("batch.",
            Collections.singletonMap("s3.data", params.get(S3_PATH)));
        Types.setupIndexAndTypes(params.get(DATA_SOURCE));
        Assets.upload(params.get(DATA_SOURCE));

        return 0;
    }

    @Override
    protected List<String> getRequiredFields() {
        return List.of(DATA_SOURCE, S3_PATH, TENANT_ID);
    }
}
