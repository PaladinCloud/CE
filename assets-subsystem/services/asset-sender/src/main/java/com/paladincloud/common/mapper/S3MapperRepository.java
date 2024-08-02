package com.paladincloud.common.mapper;

import com.paladincloud.common.aws.S3Helper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class S3MapperRepository implements MapperRepository{
    private final S3Helper s3Helper;

    @Inject
    public S3MapperRepository(S3Helper s3Helper) {
        this.s3Helper = s3Helper;
    }

    public List<String> listFiles(String base, String prefix) {
        return s3Helper.listObjects(base, prefix);
    }
    public <T> List<Map<String, T>> fetchFile(String base, String filePath) throws IOException {
        return s3Helper.fetchData(base, filePath);
    }
}
