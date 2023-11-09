package com.tmobile.pacman.api.asset.domain;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
public class NotificationRequest {

    private String searchtext = null;

    private int from;

    private int size;

    private Map<String, List<String>> filter;

    private Map<String, Object> reqFilter;

    private Map<String, Object> sortFilter;

    private String ag;
    private Date fromDate;
    private Date toDate;

    /**
     * this is used to cache the response.
     *
     * @return the key
     */
    public String getKey() {
        return ag
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
     * Gets the filter.
     *
     * @return the filter
     */
    public Map<String, List<String>> getFilter() {
        return filter;
    }

    /**
     * Sets the filter.
     *
     * @param filter the filter
     */
    public void setFilter(Map<String, List<String>>filter) {
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
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

