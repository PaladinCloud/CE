package com.tmobile.cso.pacman.datashipper.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

public class S3ClientConfig {

    private final ObjectMapper objectMapper;
    private final AmazonS3 s3Client;

    private S3ClientConfig() {
        String s3Account = System.getProperty("base.account");
        String s3Region = System.getProperty("base.region");
        String s3Role = System.getProperty("s3.role");
        objectMapper = new ObjectMapper();
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new CredentialProvider()
                        .getCredentials(s3Account, s3Role))).withRegion(s3Region).build();
    }

    private static class InstanceHolder {
        private static final S3ClientConfig instance = new S3ClientConfig();
    }

    public static S3ClientConfig getInstance() {
        return InstanceHolder.instance;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }
}
