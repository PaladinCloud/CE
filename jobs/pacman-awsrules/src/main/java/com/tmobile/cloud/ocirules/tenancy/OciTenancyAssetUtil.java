package com.tmobile.cloud.ocirules.tenancy;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helpers for tenancy-level OCI rules. These rules run on the tenancy asset (one per tenancy) and look up
 * other OCI asset types of the same tenancy in Elasticsearch, because CIS checks such as "at least one
 * compartment / notification subscription / tag default exists" can not be answered from a single asset.
 */
final class OciTenancyAssetUtil {
    static final String TENANCY_ID = "tenancyId";
    static final String LIFECYCLE_STATE = "lifecycleState";
    static final String COMPARTMENT_ID = "compartmentId";
    static final String ACTIVE = "ACTIVE";
    // The raw collector JSON is stored as primaryProvider by the current pipeline and as rawData on older documents
    private static final String[] RAW_DATA_FIELDS = {"primaryProvider", "rawData"};
    private static final String RESOURCE_ID = "_resourceid";

    private OciTenancyAssetUtil() {
    }

    /**
     * Returns the tenancy OCID of the tenancy asset being evaluated.
     */
    static String getTenancyId(Map<String, String> resourceAttributes) {
        String tenancyId = resourceAttributes.get(TENANCY_ID);
        return StringUtils.isNullOrEmpty(tenancyId) ? resourceAttributes.get(RESOURCE_ID) : tenancyId;
    }

    /**
     * Returns the raw collector JSON of every latest asset of the tenancy from the index configured in the
     * policy param urlParam, e.g. /oci_iamcompartment/_search. Falls back to the mapped document when rawData is missing.
     */
    static List<JsonObject> getLatestAssets(Map<String, String> ruleParam, String urlParam, String tenancyId) throws Exception {
        if (StringUtils.isNullOrEmpty(ruleParam.get(urlParam))) {
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION + " " + urlParam);
        }
        String esUrl = PacmanUtils.formatUrl(ruleParam, urlParam);
        if (StringUtils.isNullOrEmpty(esUrl)) {
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION + " " + PacmanRuleConstants.ES_URI);
        }

        Map<String, Object> mustFilter = new HashMap<>();
        mustFilter.put(PacmanUtils.convertAttributetoKeyword(TENANCY_ID), tenancyId);
        mustFilter.put(PacmanRuleConstants.LATEST, true);
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esUrl, mustFilter,
                new HashMap<>(), HashMultimap.create(), null, 0, new HashMap<>(), null, null);

        List<JsonObject> assets = new ArrayList<>();
        if (resultJson == null || !resultJson.has(PacmanRuleConstants.HITS)) {
            return assets;
        }
        JsonArray hits = resultJson.getAsJsonObject(PacmanRuleConstants.HITS).getAsJsonArray(PacmanRuleConstants.HITS);
        for (JsonElement hit : hits) {
            JsonElement source = hit.getAsJsonObject().get(PacmanRuleConstants.SOURCE);
            if (source == null || !source.isJsonObject()) {
                continue;
            }
            JsonObject sourceJson = source.getAsJsonObject();
            assets.add(parseRawData(sourceJson));
        }
        return assets;
    }

    private static JsonObject parseRawData(JsonObject sourceJson) {
        for (String field : RAW_DATA_FIELDS) {
            JsonElement rawData = sourceJson.get(field);
            if (rawData == null || rawData.isJsonNull()) {
                continue;
            }
            if (rawData.isJsonObject()) {
                return rawData.getAsJsonObject();
            }
            if (rawData.isJsonPrimitive()) {
                try {
                    JsonElement parsed = JsonParser.parseString(rawData.getAsString());
                    if (parsed.isJsonObject()) {
                        return parsed.getAsJsonObject();
                    }
                } catch (RuntimeException e) {
                    // try the next field, then fall back to the mapped document
                }
            }
        }
        return sourceJson;
    }

    static String getString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString().trim() : null;
    }

    static boolean isActive(JsonObject object) {
        return ACTIVE.equalsIgnoreCase(getString(object, LIFECYCLE_STATE));
    }
}
