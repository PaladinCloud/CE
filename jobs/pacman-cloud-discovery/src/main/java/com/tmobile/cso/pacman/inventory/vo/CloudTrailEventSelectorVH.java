package com.tmobile.cso.pacman.inventory.vo;

public class CloudTrailEventSelectorVH {
	

	private String readWriteType;
	private boolean includeManagementEvents;
	private String dataResourcesType;
	private String dataResourcesValue;
	
	public CloudTrailEventSelectorVH( String readWriteType, boolean includeManagementEvents,
			String dataResourcesType, String dataResourcesValue) {
		super();
		this.readWriteType = readWriteType;
		this.includeManagementEvents = includeManagementEvents;
		this.dataResourcesType = dataResourcesType;
		this.dataResourcesValue = dataResourcesValue;
	}

	public CloudTrailEventSelectorVH() {
		
	}
	public String getReadWriteType() {
		return readWriteType;
	}

	public void setReadWriteType(String readWriteType) {
		this.readWriteType = readWriteType;
	}

	

	public boolean isIncludeManagementEvents() {
		return includeManagementEvents;
	}

	public void setIncludeManagementEvents(boolean includeManagementEvents) {
		this.includeManagementEvents = includeManagementEvents;
	}

	public String getDataResourcesType() {
		return dataResourcesType;
	}

	public void setDataResourcesType(String dataResourcesType) {
		this.dataResourcesType = dataResourcesType;
	}

	public String getDataResourcesValue() {
		return dataResourcesValue;
	}

	public void setDataResourcesValue(String dataResourcesValue) {
		this.dataResourcesValue = dataResourcesValue;
	}
	
	

}
