package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.backup.model.BackupVaultListMember;

public class BackupVaultVH {
	
	/** Amazon BackupValut*/
	private BackupVaultListMember backupvault;
	
	/** AccessPolicy of BackupValut*/
	private String accessPolicy;
	
	public BackupVaultVH(BackupVaultListMember backupvault, String accessPolicy) {
		super();
		this.backupvault = backupvault;
		this.accessPolicy = accessPolicy;
	}

	public BackupVaultListMember getBackupvault() {
		return backupvault;
	}

	public void setBackupvault(BackupVaultListMember backupvault) {
		this.backupvault = backupvault;
	}

	public String getAccessPolicy() {
		return accessPolicy;
	}

	public void setAccessPolicy(String accessPolicy) {
		this.accessPolicy = accessPolicy;
	}
	
	

}
