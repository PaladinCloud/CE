package com.paladincloud.common.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paladincloud.common.assets.AssetDTO;
import java.util.List;

public class ElasticQueryAssetResponse {
    @JsonProperty("_scroll_id")
    public String scrollId;
    public Hits hits;

    public static class Hits {

        public HitsTotal total;
        public List<HitsDoc> hits;

        public static class HitsTotal {

            public long value;
            public String relation;
        }

        public static class HitsDoc {

            @JsonProperty("_index")
            public String index;
            @JsonProperty("_id")
            public String id;
            @JsonProperty("_score")
            public long score;
            @JsonProperty("_source")
            public AssetDTO source;
        }
    }

}
