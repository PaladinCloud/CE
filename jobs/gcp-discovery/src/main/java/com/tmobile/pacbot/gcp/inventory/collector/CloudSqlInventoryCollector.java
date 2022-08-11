package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.model.*;
import com.tmobile.pacbot.gcp.inventory.InventoryConstants;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.CloudSqlVH;
import com.tmobile.pacbot.gcp.inventory.vo.IPAddress;
import com.tmobile.pacbot.gcp.inventory.vo.ServerCaCert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CloudSqlInventoryCollector {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(CloudSqlInventoryCollector.class);

    public List<CloudSqlVH> fetchCloudSqlInventory(String projectId) throws IOException {
        logger.info("Running collector for cloud SQL inventory.");
        List<CloudSqlVH> cloudSqlList = new ArrayList<>();
        try {
            SQLAdmin sqlAdmin = gcpCredentialsProvider.getSqlAdminService();
            InstancesListResponse response = sqlAdmin.instances().list(projectId).execute();
            logger.info("SQL admin list api response: {}", response);
            if(response!=null && !response.isEmpty()) {
                List<DatabaseInstance> instanceList = response.getItems();
                logger.info("Database Instances list: {}", instanceList);

                if (!instanceList.isEmpty()) {

                    for (DatabaseInstance dbInstance : instanceList) {
                        logger.info("Database Instance: {}", dbInstance);
                        CloudSqlVH cloudSqlVH = new CloudSqlVH();
                        cloudSqlVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);
                        cloudSqlVH.setId(dbInstance.getConnectionName());
                        cloudSqlVH.setRegion(dbInstance.getRegion());
                        cloudSqlVH.setProjectName(projectId);

                        cloudSqlVH.setName(dbInstance.getName());
                        cloudSqlVH.setKind(dbInstance.getKind());
                        cloudSqlVH.setMasterInstanceName(dbInstance.getMasterInstanceName());
                        cloudSqlVH.setBackendType(dbInstance.getBackendType());
                        cloudSqlVH.setCreatedTime(dbInstance.getCreateTime());
                        cloudSqlVH.setState(dbInstance.getState());
                        cloudSqlVH.setDatabaseVersion(dbInstance.getDatabaseVersion());
                        cloudSqlVH.setDatabaseInstalledVersion(dbInstance.getDatabaseInstalledVersion());
                        cloudSqlVH.setInstanceType(dbInstance.getInstanceType());
                        cloudSqlVH.seteTag(dbInstance.getEtag());
                        cloudSqlVH.setSelfLink(dbInstance.getSelfLink());
                        cloudSqlVH.setServiceAccountEmail(dbInstance.getServiceAccountEmailAddress());

                        cloudSqlVH.setMaxDiskSize(dbInstance.getMaxDiskSize());
                        cloudSqlVH.setCurrentDiskSize(dbInstance.getCurrentDiskSize());
                        if (dbInstance.getDiskEncryptionConfiguration() != null) {
                            cloudSqlVH.setKmsKeyName(dbInstance.getDiskEncryptionConfiguration().getKmsKeyName());
                        }
                        setIpAddresses(cloudSqlVH, dbInstance.getIpAddresses());
                        setServerCerts(cloudSqlVH, dbInstance.getServerCaCert());
                        cloudSqlVH.setSettings(dbInstance.getSettings());

                        if (dbInstance.getDiskEncryptionStatus() != null) {
                            cloudSqlVH.setKmsKeyVersion(dbInstance.getDiskEncryptionStatus().getKmsKeyVersionName());
                        }
                        logger.info("databaseflags collecting started");
                       

                        cloudSqlList.add(cloudSqlVH);
                    }
                }
            }

        } catch (GeneralSecurityException e) {
            logger.error("Exception in connecting to cloud SQL admin service", e);
        }


        logger.info("Cloud SQL data collected list size: {}", cloudSqlList.size());
        return cloudSqlList;
    }

    private void setServerCerts(CloudSqlVH cloudSqlVH, SslCert serverCaCert) {
        ServerCaCert cert = new ServerCaCert();
        cert.setCertSerialNumber(serverCaCert.getCertSerialNumber());
        cert.setCommonName(serverCaCert.getCommonName());
        cert.setCreateTime(serverCaCert.getCreateTime());
        cert.setExpirationTime(serverCaCert.getExpirationTime());
        cert.setKind(serverCaCert.getKind());
        cert.setInstance(serverCaCert.getInstance());
        cloudSqlVH.setServerCaCert(cert);
    }

    private void setIpAddresses(CloudSqlVH cloudSqlVH, List<IpMapping> ipAddresses) {
        if (!CollectionUtils.isEmpty(ipAddresses)) {
            List<IPAddress> ipList = ipAddresses.stream().map(addr -> new IPAddress(addr.getIpAddress(), addr.getType())).collect(Collectors.toList());
            cloudSqlVH.setIpAddress(ipList);
        }
    }

}
