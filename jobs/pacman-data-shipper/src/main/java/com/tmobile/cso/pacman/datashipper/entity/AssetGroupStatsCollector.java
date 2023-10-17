package com.tmobile.cso.pacman.datashipper.entity;

import com.tmobile.cso.pacman.datashipper.dto.DatasourceData;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.AssetGroupUtil;
import com.tmobile.cso.pacman.datashipper.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.amazonaws.services.stepfunctions.builder.internal.PropertyNames.ERROR;
import static com.tmobile.pacman.commons.utils.Constants.*;

public class AssetGroupStatsCollector {
    private static final Logger log = LoggerFactory.getLogger(AssetGroupStatsCollector.class);

    private static final String AG_STATS = "assetgroup_stats";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DOMAIN = "domain";

    private static final String CURR_DATE = new SimpleDateFormat(DATE_FORMAT).format(new java.util.Date());
    // TODO: Remove this hardcoded value and eliminate the "domain" concept
    private final List<String> domains = Collections.singletonList("Infra & Platforms");
    private final List<Map<String, String>> errorList = new ArrayList<>();

    /**
     * Collect asset group stats.
     */
    public List<Map<String, String>> collectAssetGroupStats(DatasourceData datasourceData) {
        log.info("Start Collecting asset group stats");
        ESManager.createIndex(AG_STATS, errorList);
        List<String> assetGroups = datasourceData.getAssetGroups();

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(() -> {
            try {
                uploadAssetGroupCountStats(assetGroups);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupCountStats ", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupCountStats");
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        executor.execute(() -> {
            try {
                uploadAssetGroupRuleCompliance(datasourceData);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupRuleCompliance ", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupRuleCompliance");
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        executor.execute(() -> {
            try {
                uploadAssetGroupCompliance(datasourceData);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupCompliance ", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupCompliance");
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        executor.execute(() -> {
            try {
                uploadAssetGroupTagCompliance(assetGroups);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupTagCompliance", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupTagCompliance");
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        executor.execute(() -> {
            try {
                uploadAssetGroupIssues(datasourceData);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupIssues", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupIssues");
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        executor.execute(() -> {
            try {
                uploadAssetGroupVulnCompliance(assetGroups);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupVulnCompliance ", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupVulnCompliance");
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        executor.execute(() -> {
            try {
                uploadAssetListCountStats(assetGroups);
            } catch (Exception e) {
                log.error("Exception in uploadAssetListCountStats ", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetListCountStats");
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        executor.shutdown();
        while (!executor.isTerminated()) ;

        log.info("End Collecting asset group stats");
        return errorList;
    }

    /**
     * Upload asset group tag compliance.
     *
     * @param assetGroups the asset groups
     */
    public void uploadAssetGroupTagCompliance(List<String> assetGroups) {
        log.info("Start Collecting tag compliance");
        List<Map<String, Object>> docs = new ArrayList<>();
        for (String ag : assetGroups) {
            try {
                Map<String, Object> doc = AssetGroupUtil.fetchTaggingSummary(ag);
                if (!doc.isEmpty()) {
                    doc.put("ag", ag);
                    doc.put("date", CURR_DATE);
                    doc.put("@id", Util.getUniqueID(ag + "tagcompliance" + CURR_DATE));
                    docs.add(doc);
                }
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupTagCompliance", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupTagCompliance for Asset Group" + ag);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        }
        ESManager.uploadData(AG_STATS, "tagcompliance", docs, "@id", false);
        log.info("End Collecting tag compliance");
    }

    /**
     * Uploads asset group rule compliance data for the specified asset groups.
     *
     * @param datasourceData The data containing asset groups for which rule compliance data is uploaded.
     * @throws Exception If an error occurs during the upload process.
     */
    public void uploadAssetGroupRuleCompliance(DatasourceData datasourceData) {
        log.info("Start collecting Rule  compliance");
        List<Map<String, Object>> docs = new ArrayList<>();
        datasourceData.getAssetGroups().forEach((ag) -> {
            List<Map<String, Object>> docList = new ArrayList<>();
            try {
                docList = AssetGroupUtil.fetchPolicyComplianceInfo(ag, domains);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupRuleCompliance", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupRuleCompliance for Asset Group" + ag);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }

            docList.parallelStream().forEach(doc -> {
                doc.put("ag", ag);
                doc.put("date", CURR_DATE);
                doc.put("@id", Util.getUniqueID(ag + doc.get(DOMAIN) + doc.get("policyId") + CURR_DATE));
            });
            docs.addAll(docList);
        });

        ESManager.uploadData(AG_STATS, "issuecompliance", docs, "@id", false);
        log.info("End Collecting Rule  compliance");
    }

    /**
     * Uploads asset group compliance data for the specified asset groups.
     *
     * @param datasourceData The data containing asset groups for which compliance data is uploaded.
     * @throws Exception If an error occurs during the upload process.
     */
    public void uploadAssetGroupCompliance(DatasourceData datasourceData) {
        log.info("Start collecting  compliance");
        List<Map<String, Object>> docs = new ArrayList<>();
        datasourceData.getAssetGroups().forEach((ag) -> {
            try {
                List<Map<String, Object>> docList = AssetGroupUtil.fetchComplianceInfo(ag, domains);
                docList.parallelStream().forEach(doc -> {
                    doc.put("ag", ag);
                    doc.put("date", CURR_DATE);
                    doc.put("@id", Util.getUniqueID(ag + "compliance" + CURR_DATE));
                });
                docs.addAll(docList);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupCompliance ", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupCompliance for Asset Group" + ag);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        ESManager.uploadData(AG_STATS, "compliance", docs, "@id", false);
        log.info("End collecting  compliance");
    }

    /**
     * Need to collect the asset group stats and upload to ES.
     *
     * @param assetGroups the asset groups
     */
    public void uploadAssetGroupCountStats(List<String> assetGroups) {
        log.info(" Start collecting  Asset count");
        Map<String, Map<String, Map<String, Object>>> currentInfo = ESManager
                .fetchCurrentCountStatsForAssetGroups(CURR_DATE);
        List<Map<String, Object>> docs = new ArrayList<>();
        for (String ag : assetGroups) {
            try {
                List<Map<String, Object>> typeCounts = AssetGroupUtil.fetchTypeCounts(ag);
                Map<String, Map<String, Object>> currInfoMap = currentInfo.get(ag);
                typeCounts.forEach(typeCount -> {
                    String type = typeCount.get("type").toString();
                    long count = Long.valueOf(typeCount.get("count").toString());
                    long min;
                    long max;
                    if (currInfoMap != null) {
                        Map<String, Object> _minMax = currInfoMap.get(type);
                        long _min;
                        long _max;
                        if (_minMax != null) {
                            _min = Long.valueOf(_minMax.get("min").toString());
                            _max = Long.valueOf(_minMax.get("max").toString());
                        } else {
                            _min = count;
                            _max = count;
                        }
                        min = count < _min ? count : _min;
                        max = count > _max ? count : _max;

                    } else {
                        min = count;
                        max = count;
                    }
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("ag", ag);
                    doc.put("type", type);
                    doc.put("min", min);
                    doc.put("max", max);
                    doc.put("date", CURR_DATE);
                    doc.put("@id", Util.getUniqueID(ag + type + CURR_DATE + "count_type"));
                    docs.add(doc);
                });
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupCountStats", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupCountStats for Asset Group" + ag);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        }

        ESManager.uploadData(AG_STATS, "count_type", docs, "@id", false);
        log.info("End collecting  Asset count");
    }

    /**
     * Uploads asset group issues for the specified asset groups.
     *
     * @param datasourceData The data containing asset groups for which issues are uploaded.
     * @throws Exception If an error occurs during the upload process.
     */
    public void uploadAssetGroupIssues(DatasourceData datasourceData) {
        log.info("Start collecting  issues");
        List<Map<String, Object>> docs = new ArrayList<>();
        datasourceData.getAssetGroups().forEach((ag) -> {
            try {
                List<Map<String, Object>> docList = AssetGroupUtil.fetchIssuesInfo(ag, domains);
                docList.parallelStream().forEach(doc -> {
                    doc.put("ag", ag);
                    doc.put("date", CURR_DATE);
                    doc.put("@id", Util.getUniqueID(ag + "issues" + CURR_DATE));
                });

                docs.addAll(docList);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupIssues", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupIssues for Asset Group" + ag);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        });

        ESManager.uploadData(AG_STATS, "issues", docs, "@id", false);
        log.info("End collecting  issues");
    }

    /**
     * Upload asset group vuln compliance.
     *
     * @param assetGroups the asset groups
     * @throws Exception the exception
     */
    public void uploadAssetGroupVulnCompliance(List<String> assetGroups) {
        log.info("Start collecting vuln compliance");
        List<Map<String, Object>> docs = new ArrayList<>();
        for (String ag : assetGroups) {
            try {
                Map<String, Object> doc = AssetGroupUtil.fetchVulnSummary(ag);
                if (!doc.isEmpty()) {
                    doc.put("ag", ag);
                    doc.put("date", CURR_DATE);
                    doc.put("@id", Util.getUniqueID(ag + CURR_DATE + "vulncompliance"));
                    docs.add(doc);
                }
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupVulnCompliance", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupVulnCompliance for Asset Group" + ag);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        }

        ESManager.uploadData(AG_STATS, "vulncompliance", docs, "@id", false);
        log.info("End collecting vuln compliance");
    }

    public void uploadAssetListCountStats(List<String> assetGroups) {
        log.info("Collecting total asset list count data");
        List<Map<String, Object>> docs = new ArrayList<>();
        for (String ag : assetGroups) {
            log.info("Collecting asset list count for asset group: {}", ag);
            try {
                Map<String, Object> assetCountResponse = AssetGroupUtil.fetchAssetCounts(ag);
                log.info("Response from asset count API: {}", assetCountResponse);
                long totalCount = Long.valueOf(assetCountResponse.get("totalassets").toString());
                long typeCount = Long.valueOf(assetCountResponse.get("assettype").toString());
                Map<String, Object> doc = new HashMap<>();
                doc.put("ag", ag);
                doc.put("typeCount", typeCount);
                doc.put("totalassets", totalCount);
                doc.put("date", CURR_DATE);
                doc.put("@id", Util.getUniqueID(ag + CURR_DATE + "count_asset"));
                docs.add(doc);
            } catch (Exception e) {
                log.error("Exception in uploadAssetGroupCountStats", e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in uploadAssetGroupCountStats for Asset Group" + ag);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                synchronized (errorList) {
                    errorList.add(errorMap);
                }
            }
        }

        ESManager.uploadData(AG_STATS, "count_asset", docs, "@id", false);
        log.info("End of collecting total asset list count");
    }
}
