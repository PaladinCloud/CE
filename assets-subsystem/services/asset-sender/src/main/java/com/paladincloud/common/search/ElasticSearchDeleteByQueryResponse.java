package com.paladincloud.common.search;

import java.util.List;
import java.util.Map;

public class ElasticSearchDeleteByQueryResponse {
    public long total;
    public long deleted;
    public List<Map<String, Object>> failures;
}
