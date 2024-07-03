package com.paladincloud.common;

public class ProcessingDoneMessage {

    private final String jobName;
    private final String tenantId;
    private final String source;
    private final String enricherSource;

    public ProcessingDoneMessage(String jobName, String tenantId, String source,
        String enricherSource) {
        this.jobName = jobName;
        this.tenantId = tenantId;
        this.source = source;
        this.enricherSource = enricherSource;
    }

    public String getJobName() {
        return jobName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getSource() {
        return source;
    }

    public String getEnricherSource() {
        return enricherSource;
    }
}
