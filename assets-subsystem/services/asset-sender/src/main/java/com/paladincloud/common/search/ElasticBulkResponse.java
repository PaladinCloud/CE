package com.paladincloud.common.search;

import java.util.List;
import java.util.Map;

public class ElasticBulkResponse {
    public boolean errors;
    public List<Map<String, Object>> items;
}
