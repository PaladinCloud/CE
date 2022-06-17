package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.ec2.model.Snapshot;

public class SnapshotVH {

	/** EBS Snapshot */
	private Snapshot snapshot;
	
	/** snapshot is public  */
	private boolean isSnapshotPublic;

	public SnapshotVH(Snapshot snapshot, boolean isSnapshotPublic) {
		super();
		this.snapshot = snapshot;
		this.isSnapshotPublic = isSnapshotPublic;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	public boolean isSnapshotPublic() {
		return isSnapshotPublic;
	}

	public void setSnapshotPublic(boolean isSnapshotPublic) {
		this.isSnapshotPublic = isSnapshotPublic;
	}
	
	
	
}
