package com.tmobile.pacman.api.compliance.domain;

import com.google.common.base.Joiner;

import java.util.HashMap;
import java.util.Map;

public class DownloadRequest {

    /** The searchtext. */
    private String searchtext = null;

    /** The from. */
    private int from;

    /** The size. */
    private int size;

    /** The filter. */
    private Map<String, Object> filter;

    private Map<String, Object> reqFilter;

    /** The ag. */
    private String ag;

    /** The sort Filters. */
    private Map<String, Object> sortFilter;

    /** The includeDisabled. */
    private boolean includeDisabled = false;







    /**
     * this is used to cache the response.
     *
     * @return the key
     */
    public String getKey() {
        return ag
                + searchtext
                + Joiner.on("_")
                .withKeyValueSeparator("-")
                .join(filter == null ? new HashMap<String, String>()
                        : filter) + from + "" + size;
    }

    /**
     * Gets the ag.
     *
     * @return the ag
     */
    public String getAg() {
        return ag;
    }

    /**
     * Sets the ag.
     *
     * @param ag the new ag
     */
    public void setAg(String ag) {
        this.ag = ag;
    }

    /**
     * Gets the searchtext.
     *
     * @return the searchtext
     */
    public String getSearchtext() {
        return searchtext;
    }

    /**
     * Sets the searchtext.
     *
     * @param searchtext the new searchtext
     */
    public void setSearchtext(String searchtext) {
        this.searchtext = searchtext;
    }

    /**
     * Gets the from.
     *
     * @return the from
     */
    public int getFrom() {
        return from;
    }

    /**
     * Sets the from.
     *
     * @param from the new from
     */
    public void setFrom(int from) {
        this.from = from;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the size.
     *
     * @param size the new size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Gets the sort filter.
     *
     * @return the filter
     */
    public Map<String, Object> getSortFilter() {
        return sortFilter;
    }

    /**
     * Sets the sort filters.
     *
     * @param sortFilter the sort filters
     */
    public void setSortFilter(Map<String, Object> sortFilter) {
        this.sortFilter = sortFilter;
    }



    /**
     * Gets include param
     *
     * @return boolean
     */
    public boolean isIncludeDisabled() {
        return includeDisabled;
    }

    /**
     * sets include param
     *
     * @param includeDisabled includeDisabled
     */
    public void setIncludeDisabled(boolean includeDisabled) {
        this.includeDisabled = includeDisabled;
    }

    public Map<String, Object> getReqFilter() {
        return reqFilter;
    }

    public void setReqFilter(Map<String, Object> reqFilter) {
        this.reqFilter = reqFilter;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ClassPojo [ag=" + ag + ", searchtext = " + searchtext
                + ", from = " + from + ", filter = " + filter + ", size = "
                + size + "]";
    }
}
