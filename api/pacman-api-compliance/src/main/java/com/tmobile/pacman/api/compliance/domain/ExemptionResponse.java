package com.tmobile.pacman.api.compliance.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
public class ExemptionResponse {
    private List<String> failedIssueIds;
    private String Status;
    private Map<String, String> failureReason;

    public List<String> getFailedIssueIds() {
        return failedIssueIds;
    }

    public void setFailedIssueIds(List<String> failedIssueIds) {
        this.failedIssueIds = failedIssueIds;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Map<String, String> getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(Map<String, String> failureReason) {
        this.failureReason = failureReason;
    }
}
