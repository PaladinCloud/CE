package com.paladincloud.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

//@Service
public class S3Service {

    private final S3Client s3Client;

    private final String bucketName = "saasdev-temp-bucket";

    public S3Service() {
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1) // change if needed
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public String uploadByteArray(byte[] byteArray, String fileType, String serviceName) {

        String key = "download-report/" + serviceName + "_" + System.currentTimeMillis() + "." + fileType;

        String contentType = fileType.equals("csv") ? "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(byteArray));

        return key; // return S3 path
    }
}