package com.tmobile.pacman.api.compliance.service;

import com.tmobile.pacman.api.compliance.domain.IssuesException;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    void triggerCreateExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds, IssuesException issuesException);
    public void triggerRevokeExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds, String subject, String revokedBy);
}
