package com.tmobile.pacbot.gcp.inventory.vo;

public class ShieldedInstanceConfigVH {
    private boolean enableVtpm;

    private boolean enableIntegrityMonitoring;

    public boolean isEnableIntegrityMonitoring() {
        return enableIntegrityMonitoring;
    }

    public void setEnableIntegrityMonitoring(boolean enableIntegrityMonitoring) {
        this.enableIntegrityMonitoring = enableIntegrityMonitoring;
    }

    public boolean isEnableVtpm() {
        return enableVtpm;
    }

    public void setEnableVtpm(boolean enableVtpm) {
        this.enableVtpm = enableVtpm;
    }
}
