package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.gax.paging.Page;
import com.google.cloud.dns.Zone;
import com.google.api.gax.paging.Page;
import com.google.cloud.compute.v1.ZoneList;
import com.google.cloud.dns.Zone;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.CloudDNSVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
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
public class CloudDNSInventoryCollector {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(CloudDNSInventoryCollector.class);
    public List<CloudDNSVH> fetchCloudDnsInventory(ProjectVH project) throws IOException {
        List<CloudDNSVH>cloudDNSVHList=new ArrayList<>();
            Page<Zone> zonesList= gcpCredentialsProvider.createCloudDNSServices().listZones();
        logger.info("executing dns zone List collector");
            if(zonesList!=null) {
                logger.info("dns zone list {}",zonesList);

                for (Zone zone : zonesList.iterateAll()) {
                    logger.info("inside dns  zone List iterator {}",zone.getGeneratedId());
                    CloudDNSVH cloudDNSVH = new CloudDNSVH();
                    cloudDNSVH.setRegion(zone.getGeneratedId());
                    cloudDNSVH.setProjectName(project.getProjectName());
                    cloudDNSVH.setProjectId(project.getProjectId());
                    cloudDNSVH.setRegion(zone.getName());
                    cloudDNSVH.setDnsName(zone.getDnsName());
                    cloudDNSVH.setId(zone.getGeneratedId());
                    cloudDNSVH.setDnsSecConfigState(zone.getDnsSecConfig().getState());
                    cloudDNSVHList.add(cloudDNSVH);

                }
            }

        return cloudDNSVHList;
    }
}
