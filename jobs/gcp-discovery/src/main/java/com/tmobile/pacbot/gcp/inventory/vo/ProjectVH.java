package com.tmobile.pacbot.gcp.inventory.vo;

import java.util.HashMap;

public class ProjectVH extends GCPVH{
    private Long projectNumber;
    private HashMap<String,Object>computeInstanceMetadata;

    public CloudAssetVH getCloudAsset() {
        return cloudAsset;
    }

    public void setCloudAsset(CloudAssetVH cloudAsset) {
        this.cloudAsset = cloudAsset;
    }

    private CloudAssetVH cloudAsset;


    public HashMap<String, Object> getComputeInstanceMetadata() {
        return computeInstanceMetadata;
    }

    public void setComputeInstanceMetadata(HashMap<String, Object> computeInstanceMetadata) {
        this.computeInstanceMetadata = computeInstanceMetadata;
    }

    public Long getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(Long projectNumber) {
        this.projectNumber = projectNumber;
    }

}
