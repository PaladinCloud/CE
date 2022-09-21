package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;

public class DiagnosticSettingVH extends AzureVH{
    private String name;

    private List<DiagnosticSettingsLogVH> diagnosticSettingsLogVHList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DiagnosticSettingsLogVH> getDiagnosticSettingsLogVHList() {
        return diagnosticSettingsLogVHList;
    }

    public void setDiagnosticSettingsLogVHList(List<DiagnosticSettingsLogVH> diagnosticSettingsLogVHList) {
        this.diagnosticSettingsLogVHList = diagnosticSettingsLogVHList;
    }
}
