package com.tmobile.pacbot.gcp.inventory.vo;

public class NodePoolVH extends GCPVH{
    boolean autoUpgrade;
    private boolean enableIntegrityMonitoring;

    private boolean enableSecureBoot;

    public boolean isAutoUpgrade() {
        return autoUpgrade;
    }

    public void setAutoUpgrade(boolean autoUpgrade) {
        this.autoUpgrade = autoUpgrade;
    }

    public boolean isEnableIntegrityMonitoring() {
        return enableIntegrityMonitoring;
    }

    public void setEnableIntegrityMonitoring(boolean enableIntegrityMonitoring) {
        this.enableIntegrityMonitoring = enableIntegrityMonitoring;
    }

    public boolean isEnableSecureBoot() {
        return enableSecureBoot;
    }

    public void setEnableSecureBoot(boolean enableSecureBoot) {
        this.enableSecureBoot = enableSecureBoot;
    }
}