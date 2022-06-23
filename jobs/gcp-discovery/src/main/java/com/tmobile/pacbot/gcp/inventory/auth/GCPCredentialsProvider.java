package com.tmobile.pacbot.gcp.inventory.auth;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.FirewallsSettings;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;
import com.google.common.collect.Lists;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class GCPCredentialsProvider {

    private static final Logger logger = LoggerFactory.getLogger(GCPCredentialsProvider.class);

    private InstancesClient instancesClient;
    private FirewallsClient firewallsClient;

    private BigQueryOptions.Builder bigQueryBuilder;

    // If you don't specify credentials when constructing the client, the client
    // library will
    // look for credentials via the environment variable
    // GOOGLE_APPLICATION_CREDENTIALS.

    private GoogleCredentials getCredentials() throws IOException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise, credentials are read from the GOOGLE_APPLICATION_CREDENTIALS
        // environment variable.

        // print the path to the credential file
        String cred = System.getProperty("gcp.credentials");

        if (cred.isEmpty()) {
            logger.error("GCP cred string is null!!!!!!!");
        }

        return GoogleCredentials.fromStream(new ByteArrayInputStream(cred.getBytes()))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
    }

    public InstancesClient getInstancesClient() throws IOException {
        if (instancesClient == null) {
            // pass authentication credentials to the client
            InstancesSettings instancesSettings = InstancesSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials()))
                    .build();
            instancesClient = InstancesClient.create(instancesSettings);
        }
        return instancesClient;
    }

    public FirewallsClient getFirewallsClient() throws IOException {
        if (firewallsClient == null) {
            // pass authentication credentials to the client
            FirewallsSettings firewallsSettings = FirewallsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials()))
                    .build();
            firewallsClient = FirewallsClient.create(firewallsSettings);
        }
        return firewallsClient;
    }

    public BigQueryOptions.Builder getBigQueryOptions() throws IOException {
        if(bigQueryBuilder==null) {
            bigQueryBuilder = BigQueryOptions.newBuilder().setCredentials(this.getCredentials());
        }
        return bigQueryBuilder;
    }

    // close the client in destroy method
    @PreDestroy
    public void destroy() {
        if (instancesClient != null) {
            System.out.println("closing client");

            instancesClient.close();
        }
    }
}
