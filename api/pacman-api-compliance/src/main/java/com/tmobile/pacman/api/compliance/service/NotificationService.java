package com.tmobile.pacman.api.compliance.service;

import com.tmobile.pacman.api.compliance.domain.ExemptionRequest;
import com.tmobile.pacman.api.compliance.domain.IssuesException;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    void triggerCreateExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds,
                                            ExemptionRequest issuesException);
    void triggerRevokeExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds,
                                            String revokedBy);
}