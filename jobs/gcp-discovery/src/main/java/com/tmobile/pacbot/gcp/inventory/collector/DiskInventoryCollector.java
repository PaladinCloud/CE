package com.tmobile.pacbot.gcp.inventory.collector;


import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.DisksScopedList;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.DiskVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.cloud.compute.v1.DisksClient.AggregatedListPagedResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DiskInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(DiskInventoryCollector.class);

    public List<DiskVH> fetchDiskInventory(ProjectVH project) throws IOException {
        List<DiskVH> diskList = new ArrayList<>();
        logger.debug("Project id:{}",project.getProjectNumber());

        DisksClient disksClient=gcpCredentialsProvider.getDiskClient(project.getProjectId());
        AggregatedListPagedResponse aggregatedResponse = disksClient.aggregatedList(project.getProjectId());
        logger.info("Disk  entry {}", aggregatedResponse);
        for (Map.Entry<String, DisksScopedList> entry : aggregatedResponse.iterateAll()) {
            DisksScopedList disksScopedList = entry.getValue();
            if (disksScopedList.getDisksList() != null) {
                for (Disk disk : disksScopedList.getDisksList()) {
                    logger.info("Disk  iterator {}", disk);
                    DiskVH diskVH = new DiskVH();
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
            }
        }

        logger.info("Disk  list {}", diskList);

        return  diskList;
    }
}
