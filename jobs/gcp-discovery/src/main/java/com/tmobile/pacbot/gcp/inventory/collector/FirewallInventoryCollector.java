package com.tmobile.pacbot.gcp.inventory.collector;

import com.tmobile.pacbot.gcp.inventory.vo.AllowedPortsVH;
import com.tmobile.pacbot.gcp.inventory.vo.FireWallVH;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.google.cloud.compute.v1.Allowed;
import com.google.cloud.compute.v1.Firewall;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.FirewallsClient.ListPagedResponse;

@Component
public class FirewallInventoryCollector {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(FirewallInventoryCollector.class);

    public List<FireWallVH> fetchFirewallInventory(ProjectVH project) throws IOException {
        List<FireWallVH> fireWallList = new ArrayList<>();
        FirewallsClient fireWallsClient = gcpCredentialsProvider.getFirewallsClient(project.getProjectId());

        ListPagedResponse firewallResponse = fireWallsClient.list(project.getProjectId());
        logger.info("Firewall  entry {}", firewallResponse);
        for (Firewall firewall : firewallResponse.iterateAll()) {
            logger.info("Firewall  iterator {}", firewall);

            FireWallVH fireWallVH = new FireWallVH();
            fireWallVH.setName(firewall.getName());
            fireWallVH.setDisabled(firewall.getDisabled());
            fireWallVH.setDirection(firewall.getDirection());
            fireWallVH.setSourceRanges(firewall.getSourceRangesList());
            if (!firewall.getAllowedList().isEmpty()) {
                logger.info("Firewall  source Address logger {}", firewall.getAllowed(0).getIPProtocol());
                logger.info("Firewall  source Address logger {}", firewall.getAllowed(0).getPortsList());
            }
            List<AllowedPortsVH> allowedList = new ArrayList<>();
            for (Allowed allowedport : firewall.getAllowedList()) {
                AllowedPortsVH allowedportsvh = new AllowedPortsVH();
                allowedportsvh.setProtocol(allowedport.getIPProtocol());
                allowedportsvh.setPorts(allowedport.getPortsList());
                allowedList.add(allowedportsvh);
            }
            fireWallVH.setDestinationRanges(firewall.getDestinationRangesList());
            fireWallVH.setAllow(allowedList);
            fireWallVH.setId(String.valueOf(firewall.getId()));
            fireWallVH.setProjectName(project.getProjectName());
            fireWallVH.setProjectId(project.getProjectId());
            String networkRegion=firewall.getNetwork().substring(firewall.getNetwork().indexOf(project.getProjectId()+"/")+project.getProjectId().length()+1,firewall.getNetwork().indexOf("/networks"));
            fireWallVH.setRegion(networkRegion);
            fireWallList.add(fireWallVH);

            logger.info("Firewall  exit {}", fireWallVH);

        }
        logger.info("Firewall  list {}", fireWallList);

        return fireWallList;

    }

}
