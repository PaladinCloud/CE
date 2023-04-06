package com.tmobile.pacbot.gcp.inventory.auth;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.google.api.apikeys.v2.ApiKeysClient;
import com.google.api.apikeys.v2.ApiKeysSettings;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudtasks.v2.CloudTasks;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.compute.v1.*;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.cloud.container.v1.ClusterManagerSettings;
import com.google.cloud.dataproc.v1.ClusterControllerClient;
import com.google.cloud.dataproc.v1.ClusterControllerSettings;
import com.google.cloud.dns.Dns;
import com.google.cloud.dns.DnsOptions;
import com.google.cloud.functions.v1.CloudFunctionsServiceClient;
import com.google.cloud.functions.v1.CloudFunctionsServiceSettings;
import com.google.cloud.functions.v2.FunctionServiceClient;
import com.google.cloud.functions.v2.FunctionServiceSettings;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyManagementServiceSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GCPCredentialsProvider {
    @Value("${file.path}")
    private String filePath;
    @Value("${base.account}")
    private String account;
    @Value("${s3.role}")
    private String s3Role;
    @Value("${base.region}")
    private String region;
    @Value("${s3.region}")
    private String s3Region;
    @Value("${s3}")
    private String s3;

    @Value("${s3.cred.data}")
    private String s3CredData;

    private static final Logger logger = LoggerFactory.getLogger(GCPCredentialsProvider.class);

    @Autowired
    AWSCredentialProvider credProvider;

    private InstancesClient instancesClient;
    private FirewallsClient firewallsClient;

    private BigQueryOptions.Builder bigQueryBuilder;

    private TopicAdminClient topicAdminClient;

    private Storage storageClient;

    private SQLAdmin sqlAdmin;

    private KeyManagementServiceClient kmsKeyServiceClient;
    private ClusterManagerClient clusterManagerClient;
    private ZonesClient zonesClient;
    private Dns dns;
    private NetworksClient networksClient;

    private CloudResourceManager cloudResourceManager;
    private Iam iamService;
    private UrlMapsClient urlMap;
    private TargetHttpProxiesClient targetHttpProxiesClient;

    private BackendServicesClient backendService;
    private TargetSslProxiesClient targetSslProxiesClient;

    private TargetHttpsProxiesClient targetHttpsProxiesClient;

    private SslPoliciesClient sslPoliciesClient;

    private ApiKeysClient apiKeysClient;

    private Map<String, GoogleCredentials> credentialCache = new HashMap<>();

    // If you don't specify credentials when constructing the client, the client
    // library will
    // look for credentials via the environment variable
    // GOOGLE_APPLICATION_CREDENTIALS.

    private GoogleCredentials getCredentials(String projectId) throws IOException {
        // Specify a credential file by providing a path to GoogleCredentials.Otherwise, credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        logger.info("Inside getCredential method");
        if(credentialCache.containsKey(projectId)){
            //credential cache to avoid s3 read each time collector needs credential
            return credentialCache.get(projectId);
        }
        BasicSessionCredentials credentials = credProvider.getCredentials(account,region,s3Role);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard().withRegion(s3Region).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        String fileName = "gcp-credential-" + projectId + ".json";
        GetObjectRequest request=new GetObjectRequest(s3, s3CredData + File.pathSeparator + fileName);

        File credFile = new File(fileName);
        try{
            s3client.getObject(request, credFile);

            if(credFile.exists()){
                logger.info("File is created!!");
                String fileContent = Files.asCharSource(credFile, Charsets.UTF_8).read();
                GoogleCredentials gcpCredentials=GoogleCredentials.fromStream(new ByteArrayInputStream(fileContent.getBytes()))
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
                logger.info("Credentials created: {}",credentials);
                credentialCache.put(projectId,gcpCredentials);
                return  gcpCredentials;
            }else {
                logger.error("Error:: Credential file not found!! ");
            }
        }catch (Exception exc){
            logger.error("Error:: {}", exc.getMessage());
        }
        return null;

    }
    public String getAccessToken() throws IOException {
        String cred = System.getProperty("gcp.credentials");
        String token=GoogleCredentials.fromStream(new ByteArrayInputStream(cred.getBytes()))
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
                .refreshAccessToken().getTokenValue();
        return token.trim().replaceAll("\\.+$", "").toString();
    }

    public NetworksClient getNetworksClient(String projectId) throws Exception{
        if(networksClient==null){
            NetworksSettings networksSettings=NetworksSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            networksClient=NetworksClient.create(networksSettings);

        }


        return networksClient;

    }
    public InstancesClient getInstancesClient(String projectId) throws IOException {

        if (instancesClient == null) {
            // pass authentication credentials to the client
            InstancesSettings instancesSettings = InstancesSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId)))
                    .build();
            instancesClient = InstancesClient.create(instancesSettings);
        }
        return instancesClient;
    }

    public FirewallsClient getFirewallsClient(String projectId) throws IOException {
        if (firewallsClient == null) {
            // pass authentication credentials to the client
            FirewallsSettings firewallsSettings = FirewallsSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId)))
                    .build();
            firewallsClient = FirewallsClient.create(firewallsSettings);
        }
        return firewallsClient;
    }

    public BigQueryOptions.Builder getBigQueryOptions(String projectId) throws IOException {
        if (bigQueryBuilder == null) {
            bigQueryBuilder = BigQueryOptions.newBuilder().setCredentials(this.getCredentials(projectId));
        }
        return bigQueryBuilder;
    }

    public Storage getStorageClient(String projectId) throws IOException {
        if (storageClient == null) {
            storageClient = StorageOptions.newBuilder().setCredentials(this.getCredentials(projectId)).build().getService();
        }

        return storageClient;
    }

    public SQLAdmin getSqlAdminService(String projectId) throws IOException, GeneralSecurityException {
        if (sqlAdmin == null) {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            sqlAdmin = new SQLAdmin.Builder(httpTransport, jsonFactory,
                    new HttpCredentialsAdapter(this.getCredentials(projectId)))
                    .build();
        }
        return sqlAdmin;
    }

    public KeyManagementServiceClient getKmsKeyServiceClient(String projectId) throws IOException {
        if (kmsKeyServiceClient == null) {
            KeyManagementServiceSettings keyManagementServiceSettings = KeyManagementServiceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId)))
                    .build();
            kmsKeyServiceClient = KeyManagementServiceClient.create(keyManagementServiceSettings);
        }
        return kmsKeyServiceClient;
    }

    public TopicAdminClient getTopicClient(String projectId) throws IOException {
        if (topicAdminClient == null) {
            TopicAdminSettings topicAdminSettings = TopicAdminSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            topicAdminClient = TopicAdminClient.create(topicAdminSettings);
        }

        return topicAdminClient;
    }

    public FunctionServiceClient getFunctionClient(String projectId) throws IOException {
        FunctionServiceSettings functionServiceSettings=FunctionServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
        return FunctionServiceClient.create(functionServiceSettings);
    }

    public ClusterControllerClient getDataProcClient(String region, String projectId) throws IOException {
        String url = region + "-dataproc.googleapis.com:443";
        ClusterControllerSettings clusterControllerSettings = ClusterControllerSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).setEndpoint(url)
                .build();
        return ClusterControllerClient.create(clusterControllerSettings);
    }

    public CloudTasks createCloudTasksService(String projectId) throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        return new CloudTasks.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(this.getCredentials(projectId)))
                .build();
    }

    public ZonesClient Zonesclient(String projectId) throws IOException, GeneralSecurityException {
        if (zonesClient == null) {
            ZonesSettings zonesSettings = ZonesSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            zonesClient = ZonesClient.create(zonesSettings);
        }

        return zonesClient;
    }

    public ClusterManagerClient getClusterManagerClient(String projectId) throws IOException {

        if (clusterManagerClient == null) {
            ClusterManagerSettings clusterManagerSettings = ClusterManagerSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            clusterManagerClient = ClusterManagerClient.create(clusterManagerSettings);

        }
        return clusterManagerClient;
    }
    public Dns createCloudDNSServices(String projectId) throws IOException {
        if (dns == null) {
            dns = DnsOptions.newBuilder().setCredentials(this.getCredentials(projectId)).build().getService();

        }
        return dns;
    }

    public CloudResourceManager getCloudResourceManager(String projectId) throws IOException, GeneralSecurityException {
        if (cloudResourceManager == null) {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            cloudResourceManager= new CloudResourceManager.Builder(httpTransport,
                    jsonFactory, new HttpCredentialsAdapter(this.getCredentials(projectId))).build();
        }
        return cloudResourceManager;
    }

    public  Iam getIamService(String projectId) throws  IOException, GeneralSecurityException{
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        if(iamService==null){
         iamService = new Iam.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(this.getCredentials(projectId))).build();
        }
       return  iamService;
    }

    public ApiKeysClient getApiKeysService(String projectId) throws Exception{
        if(apiKeysClient==null){
            ApiKeysSettings apiKeysSettings = ApiKeysSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
             apiKeysClient=   ApiKeysClient.create(apiKeysSettings);
        }
        return apiKeysClient;
    }

    public UrlMapsClient getURLMap(String projectId) throws IOException {
        if(urlMap==null)
        {
           UrlMapsSettings urlMapsSettings= UrlMapsSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            urlMap=UrlMapsClient.create(urlMapsSettings);
        }
        return urlMap;
    }

    public TargetHttpProxiesClient getTargetHttpProxiesClient(String projectId) throws IOException {
        if (targetHttpProxiesClient == null) {
            TargetHttpProxiesSettings targetHttpProxiesSettings = TargetHttpProxiesSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            targetHttpProxiesClient = TargetHttpProxiesClient.create(targetHttpProxiesSettings);
        }
        return targetHttpProxiesClient;
    }

    public BackendServicesClient getBackendServiceClient(String projectId) throws IOException {
        if(backendService==null)
        {
           BackendServicesSettings backendServicesSettings= BackendServicesSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            backendService=BackendServicesClient.create(backendServicesSettings);
        }
        return backendService;
    }

    public TargetSslProxiesClient getTargetSslProxiesClient(String projectId) throws IOException{
        if(targetSslProxiesClient == null){
            TargetSslProxiesSettings targetSslProxiesSettings=TargetSslProxiesSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            targetSslProxiesClient=TargetSslProxiesClient.create(targetSslProxiesSettings);
        }
        return targetSslProxiesClient;
    }

    public TargetHttpsProxiesClient getTargetHttpsProxiesClient(String projectId) throws IOException{
        if(targetHttpsProxiesClient == null){
            TargetHttpsProxiesSettings targetHttpsProxiesSettings=TargetHttpsProxiesSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            targetHttpsProxiesClient=TargetHttpsProxiesClient.create(targetHttpsProxiesSettings);
        }
        return  targetHttpsProxiesClient;
    }

    public  SslPoliciesClient getSslPoliciesClient(String projectId) throws  IOException{
        if(sslPoliciesClient == null){
            SslPoliciesSettings sslPoliciesSettings=SslPoliciesSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
            sslPoliciesClient=SslPoliciesClient.create(sslPoliciesSettings);
        }
        return sslPoliciesClient;
    }



    public CloudFunctionsServiceClient getFunctionClientGen1(String projectId) throws IOException {
        CloudFunctionsServiceSettings functionsServiceSettings = CloudFunctionsServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials(projectId))).build();
        return CloudFunctionsServiceClient.create(functionsServiceSettings);
    }

    // close the client in destroy method
    @PreDestroy
    public void destroy() {
        if (instancesClient != null) {
            logger.debug("closing client");

            instancesClient.close();
        }
    }
}
