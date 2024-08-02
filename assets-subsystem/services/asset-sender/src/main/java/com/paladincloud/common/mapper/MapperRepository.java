package com.paladincloud.common.mapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MapperRepository {
    List<String> listFiles(String base, String prefix);
    <T> List<Map<String, T>> fetchFile(String base, String filePath) throws IOException;
}
