package com.paladincloud.common.config;

public interface ConfigConstants {

    interface Dev {
        String INDEX_PREFIX = "param.index-prefix";
        String ASSET_TYPE_OVERRIDE = "param.asset-type-override";
        String OMIT_DONE_EVENT = "param.omit-done-event";
    }

    interface Config {

        String TYPES_QUERY = "param.config-query";
        String TARGET_TYPE_INCLUDE = "param.target-type-include";
        String TARGET_TYPE_EXCLUDE = "param.target-type-exclude";
    }

    interface Elastic {

        String HOST = "batch.elastic-search.host";
        String PORT = "batch.elastic-search.port";
    }

    interface PaladinCloud {
        String API_AUTH_CREDENTIALS = "application.apiauthinfo";
        String AUTH_API_URL = "environment.AUTH_API_URL";
        String BASE_PALADIN_CLOUD_API_URI = "environment.BASE_PALADIN_CLOUD_API_URI";
    }

    interface RDS {

        String DB_URL = "batch.spring.datasource.url";
        String USER = "batch.spring.datasource.username";
        String PWD = "batch.spring.datasource.password";

    }

    interface S3 {

        String BUCKET_NAME = "batch.s3";
        String DATA_PATH = "batch.s3.data";
    }

    interface Sender {

        String ATTRIBUTES_TO_PRESERVE = "batch.shipper.attributes.to.preserve";
    }

    interface SQS {
        String ASSET_SHIPPER_DONE_SQS_URL = "environment.ASSET_SHIPPER_DONE_SQS_URL";
    }
}
