package com.tmobile.pacbot.azure.inventory.vo;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class BlobContainerVH extends AzureVH {

	private String name;
	private String type;
	private String tag;
	private Map<String, Object> propertiesMap;
	private Map<String, String> tags;

	private boolean hasImmutabilityPolicy;
	private boolean hasLegalHold;

	public boolean isHasLegalHold() {
		return hasLegalHold;
	}

	public void setHasLegalHold(boolean hasLegalHold) {
		this.hasLegalHold = hasLegalHold;
	}

	public boolean isHasImmutabilityPolicy() {
		return hasImmutabilityPolicy;
	}

	public void setHasImmutabilityPolicy(boolean hasImmutabilityPolicy) {
		this.hasImmutabilityPolicy = hasImmutabilityPolicy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Map<String, Object> getPropertiesMap() {
		return propertiesMap;
	}

	public void setPropertiesMap(Map<String, Object> propertiesMap) {
		this.propertiesMap = propertiesMap;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

}
