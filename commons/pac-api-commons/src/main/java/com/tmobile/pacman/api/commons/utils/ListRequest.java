package com.tmobile.pacman.api.commons.utils;

import java.util.Map;

public class ListRequest {
    private int from;
    private int size;
    private Map<String, Object> filter;
    private Map<String, Object> sortFilter;
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

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    public Map<String, Object> getSortFilter() {
        return sortFilter;
    }

    public void setSortFilter(Map<String, Object> sortFilter) {
        this.sortFilter = sortFilter;
    }

}
