package com.tmobile.cso.pacman.datashipper.util;

import java.util.List;

public class TaggingOverviewResponse {

    public Data data;

    public static class Data {
        public String ag;
        public double overallCompliancePercentage;
        public int overallTaggedCount;
        public int overallAssetCount;
        public double lastWeekCompliancePercentage;
        public double overallDelta;
        public int lastWeekTaggedCount;
        public int lastWeekAssetCount;
        public List<AssetType> assetTypes;
    }

    public static class AssetType {
        public String targetType;
        public String displayName;
        public int assetCount;
        public int taggedCount;
        public int untaggedCount;
        public double compliancePercentage;
        public double lastWeekCompliancePercentage;
        public double delta;
        public int lastWeekTaggedCount;
        public int lastWeekAssetCount;
    }
}
