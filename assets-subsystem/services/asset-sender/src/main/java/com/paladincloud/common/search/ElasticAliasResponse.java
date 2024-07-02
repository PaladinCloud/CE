package com.paladincloud.common.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ElasticAliasResponse {
    public boolean acknowledged;

    // FYI: These appear to be in the ElasticSearch 8.x response, not in the 7.x response
    public boolean errors;
    @JsonProperty("action_results")
    public List<Map<String, Object>> actionResults;
}

