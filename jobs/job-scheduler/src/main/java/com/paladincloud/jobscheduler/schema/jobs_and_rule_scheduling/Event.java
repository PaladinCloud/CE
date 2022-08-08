package com.paladincloud.jobscheduler.schema.jobs_and_rule_scheduling;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("batchNo")
    private BigDecimal batchNo = null;

    @JsonProperty("cloudName")
    private String cloudName = null;

    @JsonProperty("isCollector")
    private Boolean isCollector = null;

    @JsonProperty("isRule")
    private Boolean isRule = null;

    @JsonProperty("submitJob")
    private Boolean submitJob = null;

    @JsonProperty("isShipper")
    private Boolean isShipper = null;

    public Event batchNo(BigDecimal batchNo) {
        this.batchNo = batchNo;
        return this;
    }


    public BigDecimal getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(BigDecimal batchNo) {
        this.batchNo = batchNo;
    }

    public Event cloudName(String cloudName) {
        this.cloudName = cloudName;
        return this;
    }


    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public Event isCollector(Boolean isCollector) {
        this.isCollector = isCollector;
        return this;
    }


    public Boolean isIsCollector() {
        return isCollector;
    }

    public void setIsCollector(Boolean isCollector) {
        this.isCollector = isCollector;
    }

    public Event isRule(Boolean isRule) {
        this.isRule = isRule;
        return this;
    }


    public Boolean isIsRule() {
        return isRule;
    }

    public void setIsRule(Boolean isRule) {
        this.isRule = isRule;
    }

    public Event submitJob(Boolean submitJob) {
        this.submitJob = submitJob;
        return this;
    }


    public Boolean isSubmitJob() {
        return submitJob;
    }

    public void setSubmitJob(Boolean submitJob) {
        this.submitJob = submitJob;
    }

    public Event isShipper(Boolean isShipper) {
        this.isShipper = isShipper;
        return this;
    }


    public Boolean isIsShipper() {
        return isShipper;
    }

    public void setIsShipper(Boolean isShipper) {
        this.isShipper = isShipper;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        return Objects.equals(this.batchNo, event.batchNo) && Objects.equals(this.cloudName, event.cloudName) && Objects.equals(this.isCollector, event.isCollector) && Objects.equals(this.isRule, event.isRule) && Objects.equals(this.submitJob, event.submitJob) && Objects.equals(this.isShipper, event.isShipper);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(batchNo, cloudName, isCollector, isRule, submitJob, isShipper);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Event {\n");

        sb.append("    batchNo: ").append(toIndentedString(batchNo)).append("\n");
        sb.append("    cloudName: ").append(toIndentedString(cloudName)).append("\n");
        sb.append("    isCollector: ").append(toIndentedString(isCollector)).append("\n");
        sb.append("    isRule: ").append(toIndentedString(isRule)).append("\n");
        sb.append("    submitJob: ").append(toIndentedString(submitJob)).append("\n");
        sb.append("    isShipper: ").append(toIndentedString(isShipper)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}