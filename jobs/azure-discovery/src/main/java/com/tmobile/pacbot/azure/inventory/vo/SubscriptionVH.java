package com.tmobile.pacbot.azure.inventory.vo;

public class SubscriptionVH extends AzureVH {

	@Override
	public String toString() {
		return "{ subscriptionName=" + subscriptionName + ", subscriptionId=" + subscriptionId + ", tenant=" + tenant + "}";
	}

	private String subscriptionId;
	private String subscriptionName;
	private String tenant;

	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getSubscriptionId() {
		return subscriptionId;
	}
	public void setSubscriptionId(String subscription) {
		this.subscriptionId = subscription;
	}

	@Override
	public String getSubscriptionName() {
		return subscriptionName;
	}

	@Override
	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}
}
