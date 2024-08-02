package com.paladincloud.common.assets;

import com.paladincloud.common.AssetDocumentFields;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticBatch;
import com.paladincloud.common.search.ElasticBatch.BatchItem;
import com.paladincloud.common.search.ElasticQueryMapResponse;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import com.paladincloud.common.util.StringHelper;
import com.paladincloud.common.util.TimeHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class AssetGroupStatsCollector {

    private static final Logger LOGGER = LogManager.getLogger(AssetGroupStatsCollector.class);
    private static final String ASSET_GROUP_STATS_INDEX = "assetgroup_stats";
    private final List<String> domains = Collections.singletonList("Infra & Platforms");
    private final ElasticSearchHelper elasticSearch;
    private final AssetCountsHelper assetCountsHelper;

    @Inject
    public AssetGroupStatsCollector(ElasticSearchHelper elasticSearch, AssetCountsHelper assetCountsHelper) {
        this.elasticSearch = elasticSearch;
        this.assetCountsHelper = assetCountsHelper;
    }

    public void collectStats(List<String> assetGroups) throws Exception {
        if (assetGroups.isEmpty()) {
            return;
        }
        LOGGER.info("Collecting asset group stats");
        elasticSearch.createIndex(ASSET_GROUP_STATS_INDEX);

        var currentDate = TimeHelper.formatYearMonthDay();
        try (var executor = Executors.newCachedThreadPool()) {
            var futures = new ArrayList<Future<?>>();
            futures.add(executor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    uploadAssetGroupCountStats(currentDate, assetGroups);
                    return null;
                }
            }));

            futures.add(executor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    uploadAssetGroupRuleCompliance(currentDate, assetGroups);
                    return null;
                }
            }));

            futures.add(executor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    uploadAssetGroupCompliance(currentDate, assetGroups);
                    return null;
                }
            }));

            futures.add(executor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    uploadAssetGroupTagCompliance(currentDate, assetGroups);
                    return null;
                }
            }));

            futures.add(executor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    uploadAssetGroupIssues(currentDate, assetGroups);
                    return null;
                }
            }));

            futures.add(executor.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    uploadAssetListCountStats(currentDate, assetGroups);
                    return null;
                }
            }));

            futures.forEach(f -> {
                try {
                    f.get();
                } catch (Throwable t) {
                    throw new JobException("Failed in a job collecting stats", t);
                }
            });
        }

        LOGGER.info("Finished collecting asset group stats");
    }

    private void uploadAssetGroupCountStats(String currentDate, List<String> assetGroups)
        throws Exception {
        var currentInfo = getCurrentCounts(currentDate);

        try (var batch = new ElasticBatch(elasticSearch)) {
            for (String assetGroup : assetGroups) {
                var typeCounts = assetCountsHelper.fetchTypeCounts(assetGroup);
                var current = currentInfo.get(assetGroup);
                for (var typeCount : typeCounts) {
                    var type = typeCount.get("type").toString();
                    long count = Long.parseLong((typeCount.get("count").toString()));
                    if (current != null) {
                        long min = count;
                        long max = count;
                        var minMax = current.get("type");
                        if (minMax != null) {
                            min = Math.min(min, Long.parseLong(minMax.get("min").toString()));
                            max = Math.max(max, Long.parseLong(minMax.get("max").toString()));
                        }
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("ag", assetGroup);
                        doc.put("type", type);
                        doc.put("min", min);
                        doc.put("max", max);
                        doc.put("date", currentDate);
                        doc.put(AssetDocumentFields.DOC_TYPE, "count_type");

                        var id = StringHelper.generateSignature(
                            STR."\{assetGroup}\{type}\{currentDate}count_type");
                        doc.put("@id", id);
                        batch.add(BatchItem.documentEntry(ASSET_GROUP_STATS_INDEX, id, doc));
                    }
                }
            }
        }
    }

    private void uploadAssetGroupRuleCompliance(String currentDate, List<String> assetGroups)
        throws Exception {
        try (var batch = new ElasticBatch(elasticSearch)) {
            for (String assetGroup : assetGroups) {
                var docList = assetCountsHelper.fetchPolicyCompliance(assetGroup, domains);
                docList.parallelStream().forEach(doc -> {
                    doc.put("ag", assetGroup);
                    doc.put("date", currentDate);
                    var id = StringHelper.generateSignature(
                        STR."\{assetGroup}\{doc.get("domain")}\{doc.get(
                            "policyId")}\{currentDate}");
                    doc.put("@id", id);
                    doc.put(AssetDocumentFields.DOC_TYPE, "issuecompliance");
                });
                batch.add(docList.stream().map(
                    d -> BatchItem.documentEntry(ASSET_GROUP_STATS_INDEX, d.get("@id").toString(),
                        d)).toList());
            }
        }
    }

    private void uploadAssetGroupCompliance(String currentDate, List<String> assetGroups)
        throws Exception {
        try (var batch = new ElasticBatch(elasticSearch)) {
            for (String assetGroup : assetGroups) {
                var docList = assetCountsHelper.fetchCompliance(assetGroup, domains);
                docList.parallelStream().forEach(doc -> {
                    doc.put("ag", assetGroup);
                    doc.put("date", currentDate);
                    var id = StringHelper.generateSignature(
                        STR."\{assetGroup}compliance\{currentDate}");
                    doc.put("@id", id);
                    doc.put(AssetDocumentFields.DOC_TYPE, "compliance");
                });
                batch.add(docList.stream().map(
                    d -> BatchItem.documentEntry(ASSET_GROUP_STATS_INDEX, d.get("@id").toString(),
                        d)).toList());
            }
        }
    }

    private void uploadAssetGroupTagCompliance(String currentDate, List<String> assetGroups)
        throws Exception {
        try (var batch = new ElasticBatch(elasticSearch)) {
            var docList = new ArrayList<Map<String, Object>>();
            for (String assetGroup : assetGroups) {
                var doc = assetCountsHelper.fetchTaggingSummary(assetGroup);
                if (!doc.isEmpty()) {
                    doc.put("ag", assetGroup);
                    doc.put("date", currentDate);
                    var id = StringHelper.generateSignature(
                        STR."\{assetGroup}tagcompliance\{currentDate})");
                    doc.put("@id", id);
                    doc.put(AssetDocumentFields.DOC_TYPE, "tagcompliance");
                    docList.add(doc);
                }
            }
            batch.add(docList.stream().map(
                    d -> BatchItem.documentEntry(ASSET_GROUP_STATS_INDEX, d.get("@id").toString(), d))
                .toList());
        }
    }

    private void uploadAssetGroupIssues(String currentDate, List<String> assetGroups)
        throws Exception {
        try (var batch = new ElasticBatch(elasticSearch)) {
            for (String assetGroup : assetGroups) {
                var docList = assetCountsHelper.fetchIssuesInfo(assetGroup, domains);
                docList.parallelStream().forEach(doc -> {
                    doc.put("ag", assetGroup);
                    doc.put("date", currentDate);
                    var id = StringHelper.generateSignature(
                        STR."\{assetGroup}issues\{currentDate}");
                    doc.put("@id", id);
                    doc.put(AssetDocumentFields.DOC_TYPE, "issues");
                });

                batch.add(docList.stream().map(
                    d -> BatchItem.documentEntry(ASSET_GROUP_STATS_INDEX, d.get("@id").toString(),
                        d)).toList());
            }
        }
    }

    private void uploadAssetListCountStats(String currentDate, List<String> assetGroups)
        throws Exception {
        try (var batch = new ElasticBatch(elasticSearch)) {
            for (String assetGroup : assetGroups) {
                var assetCounts = assetCountsHelper.fetchAssetCounts(assetGroup);
                var doc = new HashMap<String, Object>();
                doc.put("ag", assetGroup);
                doc.put("date", currentDate);
                var id = StringHelper.generateSignature(
                    STR."\{assetGroup}\\{currentDate}count_asset");
                doc.put("@id", id);
                doc.put(AssetDocumentFields.DOC_TYPE, "count_asset");
                doc.put("typeCount", assetCounts.get("assettype"));
                doc.put("totalAssets", assetCounts.get("totalassets"));

                batch.add(BatchItem.documentEntry(ASSET_GROUP_STATS_INDEX, id, doc));
            }
        }
    }

    private Map<String, Map<String, Map<String, Object>>> getCurrentCounts(String date)
        throws IOException {
        var payload = STR."""
            {
                "query": {
                    "bool": {
                        "must": [
                            {
                                "match": {
                                    "docType": "count_type"
                                }
                            },
                            {
                                "match": {
                                    "date": "\{date}"
                                }
                            }
                        ]
                    }
                }
            }
            """.trim();
        var response = elasticSearch.invokeCheckAndConvert(ElasticQueryMapResponse.class,
            HttpMethod.POST, STR."\{ASSET_GROUP_STATS_INDEX}/_search?size=10000", payload);
        Map<String, Map<String, Map<String, Object>>> infoList = new HashMap<>();
        var docs = response.hits.hits.stream().map(h -> h.source).toList();
        for (var doc : docs) {
            var assetGroup = doc.get("ag").toString();
            var typeInfo = infoList.get(assetGroup);
            if (typeInfo == null) {
                typeInfo = new HashMap<>();
                infoList.put(assetGroup, typeInfo);
            }
            typeInfo.put(doc.get("type").toString(), doc);
            doc.remove("ag");
            doc.remove("type");
        }

        return infoList;
    }
}
