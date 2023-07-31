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
}
