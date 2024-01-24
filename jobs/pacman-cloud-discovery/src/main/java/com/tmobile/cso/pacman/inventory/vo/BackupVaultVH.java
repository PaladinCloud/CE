package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.backup.model.BackupVaultListMember;

public class BackupVaultVH {

    /**
     * Amazon BackupVault
     */
    private BackupVaultListMember backupVault;

    /**
     * AccessPolicy of BackupVault
     */
    private String accessPolicy;

    public BackupVaultVH(BackupVaultListMember backupVault, String accessPolicy) {
        super();
        this.backupVault = backupVault;
        this.accessPolicy = accessPolicy;
    }

    public BackupVaultListMember getBackupVault() {
        return backupVault;
    }

    public void setBackupVault(BackupVaultListMember backupVault) {
        this.backupVault = backupVault;
    }

    public String getAccessPolicy() {
        return accessPolicy;
    }

    public void setAccessPolicy(String accessPolicy) {
        this.accessPolicy = accessPolicy;
    }
}
