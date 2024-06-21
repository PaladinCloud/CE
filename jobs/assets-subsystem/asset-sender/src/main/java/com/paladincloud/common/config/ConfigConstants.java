package com.paladincloud.common.config;

public interface ConfigConstants {

    interface Config {

        String CONFIG_TYPES_QUERY = "param.config-query";
        String CONFIG_TARGET_TYPE_INCLUDE = "param.target-type-include";
        String CONFIG_TARGET_TYPE_EXCLUDE = "param.target-type-exclude";
    }

    interface Elastic {

        String ELASTIC_HOST = "batch.elastic-search.host";
        String ELASTIC_PORT = "batch.elastic-search.port";
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
}
