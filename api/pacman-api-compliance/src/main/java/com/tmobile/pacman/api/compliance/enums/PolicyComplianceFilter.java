package com.tmobile.pacman.api.compliance.enums;

public enum PolicyComplianceFilter {

    PROVIDER("provider"),
    POLICY_NAME("name"),
    CATEGORY("policyCategory"),
    SEVERITY("severity"),
    ASSET_TYPE("resourcetType"),
    SOURCE("provider"),
    COMPLIANCE("compliance_percent"),
    VIOLATIONS("failed");

    public final String filter;

    private PolicyComplianceFilter(String filter) {
        this.filter = filter;
    }
}
