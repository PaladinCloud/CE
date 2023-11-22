package com.tmobile.pacman.api.admin.domain;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PluginRequestBody {

    private String searchtext = null;

    private String attributeName;

    private int page;

    private int size;

    private Map<String, Object> filter;

    private Map<String, String> sortFilter;

    private String ag;

    /**
     * this is used to cache the response.
     *
     * @return the key
     */
    public String getKey() {
        return ag
                + searchtext
                + Joiner.on("_").withKeyValueSeparator("-")
                .join(filter == null ? new HashMap<String, String>() : filter) + page + "" + size;
    }


}
