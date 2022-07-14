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

@Component
public class GCPlocationUtil {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final String PROJECT_PREFIX="projects/";

    public List<String> getLocations(String projectName) throws GeneralSecurityException, IOException {
        CloudTasks cloudTasksService =gcpCredentialsProvider.createCloudTasksService();
        CloudTasks.Projects.Locations.List request =
                cloudTasksService.projects().locations().list(PROJECT_PREFIX+projectName);

        ListLocationsResponse response;
        List<String> locations=new ArrayList<>();
        do {
            response = request.execute();
            if (response.getLocations() == null) {
                continue;
            }
            for(Location l:response.getLocations()){
                locations.add(l.getLocationId());
            }
            request.setPageToken(response.getNextPageToken());
        } while (response.getNextPageToken() != null);
        locations.add("us");
        locations.add("global");
        return locations;
    }
}
