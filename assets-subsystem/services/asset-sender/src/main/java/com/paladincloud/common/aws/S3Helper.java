package com.paladincloud.common.aws;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Singleton
public class S3Helper {

    private static final Logger LOGGER = LogManager.getLogger(S3Helper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public S3Helper() {
    }

    private S3Client s3Client() {
        return S3Client.builder().httpClientBuilder(ApacheHttpClient.builder()).build();
    }

    public List<String> listObjects(String bucket, String prefix) {
        try (var s3Client = s3Client()) {
            List<String> result = new ArrayList<>();
            var pager = s3Client.listObjectsV2Paginator(
                ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build());
            for (var page : pager) {
                result.addAll(page.contents().stream().map(S3Object::key).toList());
            }
            return result;
        }
    }

    public <T> List<Map<String, T>> fetchData(String bucket, String path) throws IOException {
        try (var s3Client = s3Client()) {
            if (!doesObjectExist(s3Client, bucket, path)) {
                LOGGER.info("File '{}' does not exist in bucket '{}'", path, bucket);
                return new ArrayList<>();
            }
            var response = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(path).build());
            return objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .readValue(response.asUtf8String(), new TypeReference<>() {
                });
        }
    }

    private boolean doesObjectExist(S3Client s3Client, String bucket, String path) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(path).build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}
