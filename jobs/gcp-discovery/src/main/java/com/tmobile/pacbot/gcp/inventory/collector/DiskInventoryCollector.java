package com.tmobile.pacbot.gcp.inventory.collector;


import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.ListDisksRequest;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.DiskVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.cloud.compute.v1.DisksClient.ListPagedResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiskInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(DiskInventoryCollector.class);

    public List<DiskVH> fetchDiskInventory(ProjectVH project) throws IOException {
        List<DiskVH> diskList = new ArrayList<>();
        logger.debug("Project id:{}",project.getProjectNumber());

        DisksClient disksClient=gcpCredentialsProvider.getDiskClient(project.getProjectId());
        ListDisksRequest request = ListDisksRequest.newBuilder()
                .setProject(project.getProjectId())
                .build();
        ListPagedResponse diskResponse = disksClient.list(request);
        logger.info("Disk  entry {}", diskResponse);
        for (Disk disk : diskResponse.iterateAll()) {
            logger.info("Disk  iterator {}", disk);

            DiskVH diskVH=new DiskVH();
            diskVH.setName(disk.getName());
            diskVH.setKind(disk.getKind());
            diskVH.setSizeGb(disk.getSizeGb());
            diskVH.setZone(disk.getZone());
            diskVH.setStatus(disk.getStatus());
            diskVH.setType(disk.getType());
            diskVH.setId(String.valueOf(disk.getId()));
            diskVH.setProjectName(project.getProjectName());
            diskVH.setProjectId(project.getProjectId());
            diskVH.setLicenses(disk.getLicensesList());
            diskVH.setUsers(disk.getUsersList());
            diskVH.setLicenseCodes(disk.getLicenseCodesList());
            diskList.add(diskVH);

            logger.info("Disk  exit {}", diskVH);
        }

        logger.info("Disk  list {}", diskList);

        return  diskList;
    }
}
