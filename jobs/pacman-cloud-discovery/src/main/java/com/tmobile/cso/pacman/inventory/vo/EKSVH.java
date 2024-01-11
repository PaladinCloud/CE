package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.eks.model.Cluster;
import com.amazonaws.services.workspaces.model.Tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;


/**
 * The Class LambdaVH.
 */
public class EKSVH {

    /**
     * The lambda.
     */
    private final Cluster cluster;

    /**
     * The tags.
     */
    private final List<Tag> tags;

    /**
     * Instantiates a new lambda VH.
     *
     * @param cluster   the cluster
     */
    public EKSVH(Cluster cluster) {
        this.cluster = cluster;
        this.tags = new ArrayList<>();
        if (cluster != null && cluster.getTags() != null && !cluster.getTags().isEmpty()) {
            Iterator<Entry<String, String>> it = cluster.getTags().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                Tag tag = new Tag();
                tag.setKey(entry.getKey());
                tag.setValue(entry.getValue());
                tags.add(tag);
            }
        }
    }
}
