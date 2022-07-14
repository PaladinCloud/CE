package com.tmobile.pacbot.gcp.inventory.auth;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.cloudtasks.v2.CloudTasks;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.FirewallsSettings;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSettings;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyManagementServiceSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import com.google.common.collect.Lists;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class GCPCredentialsProvider {

    private static final Logger logger = LoggerFactory.getLogger(GCPCredentialsProvider.class);

    private InstancesClient instancesClient;
    private FirewallsClient firewallsClient;

    private BigQueryOptions.Builder bigQueryBuilder;

    private TopicAdminClient topicAdminClient;

    private Storage storageClient;

    private SQLAdmin sqlAdmin;

    private KeyManagementServiceClient kmsKeyServiceClient;

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
        if (bigQueryBuilder == null) {
            bigQueryBuilder = BigQueryOptions.newBuilder().setCredentials(this.getCredentials());
        }
        return bigQueryBuilder;
    }

    public Storage getStorageClient() throws IOException {
        if (storageClient == null) {
            storageClient = StorageOptions.newBuilder().setCredentials(this.getCredentials()).build().getService();
        }

        return storageClient;
    }

    public SQLAdmin getSqlAdminService() throws IOException, GeneralSecurityException {
        if (sqlAdmin == null) {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            sqlAdmin = new SQLAdmin.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(this.getCredentials()))
                    .build();
        }
        return sqlAdmin;
    }


    public KeyManagementServiceClient getKmsKeyServiceClient() throws IOException {
        if (kmsKeyServiceClient == null) {
            KeyManagementServiceSettings keyManagementServiceSettings = KeyManagementServiceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials()))
                    .build();
            kmsKeyServiceClient = KeyManagementServiceClient.create(keyManagementServiceSettings);
        }
        return kmsKeyServiceClient;
    }

    public TopicAdminClient getTopicClient() throws IOException {
        if(topicAdminClient==null) {
            TopicAdminSettings topicAdminSettings=TopicAdminSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials())).build();
            topicAdminClient =TopicAdminClient.create(topicAdminSettings);
        }
        return topicAdminClient;
    }

    public CloudTasks createCloudTasksService() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        return new CloudTasks.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(this.getCredentials()))
                .build();
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
