package com.paladincloud.common.search;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class ElasticAliasRequest {

    public List<Add> actions;

    public String toSerializedFormat() {
        return STR."""
            "actions": [\{StringUtils.join(actions, ",\n")}]
            """.trim();
    }

    interface Action {

        String toSerializedFormat();
    }

    public record Add(String index, String alias) implements Action {

        @Override
        public String toSerializedFormat() {
            return STR."""
                    "{ add": { "index": "\{index}, "alias": "\{alias} } }"
                    """.trim();
        }
    }
}
