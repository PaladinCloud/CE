package com.tmobile.pacbot.gcp.inventory.util;

import com.google.api.services.cloudtasks.v2.CloudTasks;
import com.google.api.services.cloudtasks.v2.model.ListLocationsResponse;
import com.google.api.services.cloudtasks.v2.model.Location;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.compute.v1.Zone;
import com.google.cloud.compute.v1.ZoneList;
import com.google.cloud.compute.v1.ZonesClient;
import com.google.cloud.compute.v1.ZonesClient.ListPagedResponse;

@Component
public class GCPlocationUtil {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final String PROJECT_PREFIX = "projects/";
    List<String> locations = new ArrayList<>();
    List<String> zonesList = new ArrayList<>();

    public List<String> getZoneList(String projectName) throws GeneralSecurityException, IOException {
        if (zonesList.isEmpty()) {
            ZonesClient zoneClient = gcpCredentialsProvider.Zonesclient();

            ListPagedResponse zoneList = zoneClient.list(projectName);
            for (Zone zone : zoneList.iterateAll()) {
                zonesList.add(zone.getName());

            }
        }

        return zonesList;
    }

    public List<String> getLocations(String projectName) throws GeneralSecurityException, IOException {
        if (locations.isEmpty()) {
            CloudTasks cloudTasksService = gcpCredentialsProvider.createCloudTasksService();
            CloudTasks.Projects.Locations.List request = cloudTasksService.projects().locations()
                    .list(PROJECT_PREFIX + projectName);

            ListLocationsResponse response;

            do {
                response = request.execute();
                if (response.getLocations() == null) {
                    continue;
                }
                for (Location l : response.getLocations()) {
                    locations.add(l.getLocationId());
                }
                request.setPageToken(response.getNextPageToken());
            } while (response.getNextPageToken() != null);
            locations.add("us");
            locations.add("global");
        }
        return locations;
    }
}
