package com.tmobile.cso.pacman.datashipper.dto;


public class JobSchedulerSQSMessageBody {
    private String jobName;
    private String paladinCloudTenantId;
    private String source;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getPaladinCloudTenantId() {
        return paladinCloudTenantId;
    }

    public void setPaladinCloudTenantId(String paladinCloudTenantId) {
        this.paladinCloudTenantId = paladinCloudTenantId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public JobSchedulerSQSMessageBody(String jobName, String paladinCloudTenantId, String source) {
        this.jobName = jobName;
        this.paladinCloudTenantId = paladinCloudTenantId;
        this.source = source;
    }
}
