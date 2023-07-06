package com.tmobile.pacman.api.admin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FetchActivityLogsRequest {
    private String order;
    @JsonProperty("sortby")
    private String sortBy;
    @JsonProperty("searchtxt")
    private String searchText;
    private Integer size;
    @JsonProperty("fromno")
    private Integer  fromNo;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonProperty("fromdate")
    private LocalDateTime fromDate;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonProperty("todate")
    private LocalDateTime toDate;

    private Map<String, String> filter=new HashMap<>();

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getFromNo() {
        return fromNo;
    }

    public void setFromNo(Integer fromNo) {
        this.fromNo = fromNo;
    }



    public Map<String, String> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }
}
