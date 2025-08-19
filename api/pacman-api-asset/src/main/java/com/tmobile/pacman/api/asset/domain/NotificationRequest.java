package com.tmobile.pacman.api.asset.domain;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
public class NotificationRequest {

    private String searchtext = null;

    private int from;

    private int size;

    private Map<String, List<String>> filter;

    private Map<String, Object> reqFilter;

    private Map<String, Object> sortFilter;

    @JsonProperty("ag")
    private String assetGroup;
    private Date fromDate;
    private Date toDate;

    public String getKey() {
        return assetGroup
                + searchtext
                + Joiner.on("_").withKeyValueSeparator("-")
                .join(filter == null ? new HashMap<String, String>() : filter) + from + "" + size;
    }

    public Map<String, Object> getSortFilter() {
        return sortFilter;
    }

    public void setSortFilter(Map<String, Object> sortFilter) {
        this.sortFilter = sortFilter;
    }

    @JsonProperty("ag")
    public String getAssetGroup() {
        return assetGroup;
    }

    public void setAssetGroup(String assetGroup) {
        this.assetGroup = assetGroup;
    }

    public String getSearchtext() {
        return searchtext;
    }

    public void setSearchtext(String searchtext) {
        this.searchtext = searchtext;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<String, List<String>> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, List<String>>filter) {
        this.filter = filter;
    }


    @Override
    public String toString() {
        return "ClassPojo [searchtext = " + searchtext + ", from = " + from + ", filter = " + filter + ", size = "
                + size + "]";
    }

    public Map<String, Object> getReqFilter() {
        return reqFilter;
    }

    public void setReqFilter(Map<String, Object> reqFilter) {
        this.reqFilter = reqFilter;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}

