package com.paladincloud.common.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.aws.S3Helper;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import com.paladincloud.common.search.ElasticSearchUpdateByQueryResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadErrors {

    private static final Logger LOGGER = LogManager.getLogger(LoadErrors.class);
    private final S3Helper s3;
    private final ElasticSearchHelper elasticSearch;
    private final String bucket;
    private final List<String> filenames;
    private Map<String, List<Map<String, Object>>> typeToError = null;

    @Inject
    public LoadErrors(S3Helper s3, ElasticSearchHelper elasticSearch, String bucket,
        List<String> filenames) {
        this.s3 = s3;
        this.elasticSearch = elasticSearch;
        this.bucket = bucket;
        this.filenames = filenames;
    }

    public void process(String indexName, String type, String loadDate) throws IOException {
        fetchLoadErrors();
        if (typeToError.containsKey(type) || typeToError.containsKey("all")) {
            var errors = typeToError.get(type);
            if (errors == null) {
                errors = typeToError.get("all");
            }

            var query = new StringBuilder(2048);
            query.append(STR."""
                { "script": {"inline": "ctx._source._loaddate= '\{loadDate}'" },
                "query": {"bool": { "should": [
                """.trim());

            var shouldQuery = new ArrayList<String>();
            errors.forEach(err -> {
                var accountId = err.get("accountid").toString();
                var region = err.get("region").toString();
                if (StringUtils.isNotEmpty(accountId) && StringUtils.isNotEmpty(region)) {
                    shouldQuery.add(STR."""
                        {
                            "bool": {
                                "must": [
                                    {
                                        "term": {
                                            "accountid.keyword": "\{accountId}"
                                        }
                                    },
                                    {
                                        "term": {
                                            "region.keyword": "\{region}"
                                        }
                                    }
                                ]
                            }
                        }
                        """.trim());
                }
            });

            query.append(String.join(",", shouldQuery));
            query.append("]");

            var arrayOpen = false;
            if (StringUtils.isNotEmpty(type)) {
                query.append(STR."""
                    ,
                    "minimum_should_match": 1,
                    "must": [{ "match": { "docType.keyword": "\{type}" }}
                    """.trim());
                arrayOpen = true;
            }

            query.append("""
                ,{ "match": { "latest": true }}
                """.trim());
            if (arrayOpen) {
                query.append("]");
            }
            query.append("}}}");

            var response = elasticSearch.invokeCheckAndConvert(ElasticSearchUpdateByQueryResponse.class, HttpMethod.POST,
                STR."\{indexName}/_update_by_query", query.toString());
            LOGGER.info("Updated via load errors: {} {} - updateCount={}", indexName, type, response.updated);
        }
    }

    private void fetchLoadErrors() throws IOException {
        if (null == typeToError) {
            if (filenames.size() > 1) {
                throw new JobException(
                    STR."Cannot handle more than one load error file: \{filenames}");
            }
            if (filenames.isEmpty()) {
                typeToError = new HashMap<>();
            } else {
                var documents = s3.fetchData(bucket, filenames.getFirst());
                typeToError = documents.parallelStream()
                    .collect(Collectors.groupingBy(d -> d.get("type").toString()));
            }
        }
    }
}
