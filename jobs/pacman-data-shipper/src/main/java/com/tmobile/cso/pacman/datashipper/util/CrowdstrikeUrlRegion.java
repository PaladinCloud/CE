package com.tmobile.cso.pacman.datashipper.util;

import com.amazonaws.util.StringUtils;

import java.util.Arrays;

public enum CrowdstrikeUrlRegion {
    US_1("api.crowdstrike.com", "us-1"), US_2("api.us-2.crowdstrike.com", "us-2"), EU_1("api.eu-1.crowdstrike.com", "eu-1"), UG_GOV_1("api.laggar.gcw.crowdstrike.com", "us-gov-1"), US_GOV_2("api.falcon.us-gov-2.crowdstrike.mil", "us-gov-2");


    String baseUrl;
    String region;

    CrowdstrikeUrlRegion(String baseUrl, String region) {
        this.baseUrl = baseUrl;
        this.region = region;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getRegion() {
        return region;
    }

    public static CrowdstrikeUrlRegion findByUrl(String url) {
        if (StringUtils.isNullOrEmpty(url)) {
            return null;
        }
        url.substring(url.indexOf("api."));
        return Arrays.stream(values()).filter(it -> it.baseUrl.equalsIgnoreCase(url.substring(url.indexOf("api.")))).findFirst().orElse(null);
    }
}
