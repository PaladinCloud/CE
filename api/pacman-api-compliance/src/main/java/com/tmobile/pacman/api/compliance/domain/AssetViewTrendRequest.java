package com.tmobile.pacman.api.compliance.domain;

import java.util.Date;
import java.util.Map;

public class AssetViewTrendRequest {

    /** The ag. */
    private String ag;

    /** The from. */
    private Date from;

    private Date to;

    /** The filters. */
    private Map<String, String> filters;

    private String category;

    /**
     * Gets the filters.
     *
     * @return the filters
     */
    public Map<String, String> getFilters() {
        return filters;
    }

    /**
     * Sets the filters.
     *
     * @param filters the filters
     */
    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
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
     * Gets the from.
     *
     * @return the from
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Sets the from.
     *
     * @param from the new from
     */
    public void setFrom(Date from) {
        this.from = from;
    }
    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
