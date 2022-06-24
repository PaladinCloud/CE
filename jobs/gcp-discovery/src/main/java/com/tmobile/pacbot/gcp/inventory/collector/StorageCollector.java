package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.StorageVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StorageCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(StorageCollector.class);

    public List<StorageVH> fetchStorageInventory(String project) throws IOException {
        logger.debug("StorageCollector started");
        List<StorageVH> storageList = new ArrayList<>();
        Storage storageClient = gcpCredentialsProvider.getStorageClient();

        Page<Bucket> buckets = storageClient.list();

        Set<String> users=new HashSet<>();
        for (Bucket bucket : buckets.iterateAll()) {
            StorageVH storageVH=new StorageVH();
            logger.debug("Collecting acl");
            List<Acl> acls =bucket.listAcls();
            for (Acl acl : acls) {
                users.add(acl.getEntity().toString());
            }
            logger.debug("Collection done");
            storageVH.setUsers(users);
            storageVH.setId(bucket.getGeneratedId());
            storageVH.setProjectName(project);
            storageList.add(storageVH);

        }
        logger.debug("StorageCollector ended");
        return storageList;
    }
}
