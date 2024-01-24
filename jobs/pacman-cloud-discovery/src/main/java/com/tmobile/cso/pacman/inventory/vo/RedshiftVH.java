package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.redshift.model.Cluster;

import java.util.List;

public class RedshiftVH {

    Cluster cluster;
    List<String> subnets;

    public RedshiftVH(Cluster cluster) {
        this.cluster = cluster;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public List<String> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<String> subnets) {
        this.subnets = subnets;
    }
}
