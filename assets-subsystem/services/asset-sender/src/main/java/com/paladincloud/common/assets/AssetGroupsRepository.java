package com.paladincloud.common.assets;

import java.util.List;
import java.util.Map;

public interface AssetGroupsRepository {

    List<Map<String, Object>> fetchTypeCounts(String assetGroup) throws Exception;
    List<Map<String, Object>> fetchPolicyCompliance(String assetGroup, List<String> domains)
        throws Exception;
    List<Map<String, Object>> fetchCompliance(String assetGroup, List<String> domains)
        throws Exception;
    Map<String, Object> fetchTaggingSummary(String assetGroup) throws Exception;
    List<Map<String, Object>> fetchIssuesInfo(String assetGroup, List<String> domains)
        throws Exception;
    Map<String, Object> fetchAssetCounts(String assetGroup) throws Exception;
    int fetchAccountAssetCount(String platform, String accountId) throws Exception;
    void createDefaultGroup(String dataSource);
    void updateImpactedAliases(List<String> aliases, String dataSource);
}
