package com.tmobile.cso.pacman.datashipper.util;

import java.util.List;

public class TaggingSummaryResponse {

    public Data data;

    public static class Data {
        public String ag;
        public int totalAssets;
        public double overallCompliancePercentage;
        public int overallTaggedCount;
        public int overallAssetCount;
        public String description;
        public List<AssetGroupEntry> assetgroups;
    }

    public static class AssetGroupEntry {
        public String ag;
        public Stats stats;
    }

    public static class Stats {
        public int totalAssets;
        public double overallCompliancePercentage;
        public int overallTaggedCount;
        public int overallAssetCount;
        public String description;
        public List<AssetType> assetTypes;
    }

    public static class AssetType {
        public String targetType;
        public String displayName;
        public int assetCount;
        public int taggedCount;
        public int untaggedCount;
        public double compliancePercentage;
        public List<TagDetail> tagDetails;
    }

    public static class TagDetail {
        public String tagName;
        public int count;
        public double tagCompliancePercentage;
    }
}