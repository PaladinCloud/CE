package com.tmobile.cloud.gcprules.utils;

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.cloud.awsrules.utils.RulesElasticSearchRepositoryUtil;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GCPUtils {
    private static final Logger logger = LoggerFactory.getLogger(GCPUtils.class);

    private GCPUtils() {
    }

    public static JsonArray getHitsArrayFromEs(String esURL, Map<String, Object> mustFilter) throws Exception {
        logger.info("Validating the resource data from elastic search. ES URL:{}, FilterMap : {}", esURL, mustFilter);
        JsonArray hitsJsonArray = new JsonArray();
        JsonObject resultJson = RulesElasticSearchRepositoryUtil.getQueryDetailsFromES(esURL, mustFilter,
                new HashMap<>(),
                HashMultimap.create(), null, 0, new HashMap<>(), null, null);
        logger.debug("Data fetched from elastic search. Response JSON: {}", resultJson.toString());

        if (resultJson.has(PacmanRuleConstants.HITS)) {
            String hitsString = resultJson.get(PacmanRuleConstants.HITS).toString();
            logger.debug("hit content in result json: {}", hitsString);
            JsonObject hitsJson = (JsonObject) JsonParser.parseString(hitsString);
            hitsJsonArray = hitsJson.getAsJsonObject().get(PacmanRuleConstants.HITS).getAsJsonArray();
        }
        return hitsJsonArray;
    }
}
