package com.tmobile.pacbot.azure.inventory.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.LoadBalancingRule;

import java.util.List;
import java.util.Map;

@JsonSerialize
public class LoadBalancerVH extends AzureVH {

    private int hashCode;
    private String name;

    private String key;
    private LoadBalancer refresh;

    private String regionName;
    private String type;
    private List<String> publicIPAddressIds;
    private Map<String, String> tags;
    private Map<String, LoadBalancingRule> loadBalancingRules;
    private Map<String, LoadBalancerPrivateFrontend> privateFrontends;
    private Map<String, LoadBalancerPublicFrontend> publicFrontends;
    private List<String> backendPoolInstances;

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, LoadBalancingRule> getLoadBalancingRules() {
        return loadBalancingRules;
    }

    public void setLoadBalancingRules(Map<String, LoadBalancingRule> loadBalancingRules) {
        this.loadBalancingRules = loadBalancingRules;
    }

    public Map<String, LoadBalancerPrivateFrontend> getPrivateFrontends() {
        return privateFrontends;
    }

    public void setPrivateFrontends(Map<String, LoadBalancerPrivateFrontend> privateFrontends) {
        this.privateFrontends = privateFrontends;
    }

    public Map<String, LoadBalancerPublicFrontend> getPublicFrontends() {
        return publicFrontends;
    }

    public void setPublicFrontends(Map<String, LoadBalancerPublicFrontend> publicFrontends) {
        this.publicFrontends = publicFrontends;
    }

    public List<String> getPublicIPAddressIds() {
        return publicIPAddressIds;
    }

    public void setPublicIPAddressIds(List<String> publicIPAddressIds) {
        this.publicIPAddressIds = publicIPAddressIds;
    }

    public LoadBalancer getRefresh() {
        return refresh;
    }

    public void setRefresh(LoadBalancer refresh) {
        this.refresh = refresh;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getBackendPoolInstances() {
        return backendPoolInstances;
    }

    public void setBackendPoolInstances(List<String> backendPoolInstances) {
        this.backendPoolInstances = backendPoolInstances;
    }

}
