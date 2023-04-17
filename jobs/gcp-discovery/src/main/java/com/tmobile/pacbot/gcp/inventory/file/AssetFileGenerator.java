package com.tmobile.pacbot.gcp.inventory.file;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.collector.*;
import com.tmobile.pacbot.gcp.inventory.util.CloudSqlFilter;
import com.tmobile.pacbot.gcp.inventory.util.DataBaseTypeEnum;
import com.tmobile.pacbot.gcp.inventory.vo.CloudSqlVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AssetFileGenerator {

	@Autowired
	GCPCredentialsProvider gcpCredentialsProvider;
	/**
	 * The target types.
	 */
	@Value("${targetTypes:}")
	private String targetTypes;

	/**
	 * The log.
	 */
	private static final Logger log = LoggerFactory.getLogger(AssetFileGenerator.class);

	@Autowired
	VMInventoryCollector vmInventoryCollector;
	@Autowired
	FirewallInventoryCollector firewallInventoryCollector;
	@Autowired
	StorageCollector storageInventoryCollector;
	@Autowired
	CloudSqlInventoryCollector cloudSqlInventoryCollector;

	@Autowired
	BigQueryInventoryCollector bigQueryInventoryCollector;

	@Autowired
	PubSubInventoryCollector pubSubInventoryCollector;

	@Autowired
	KmsKeyInventoryCollector kmsKeyInventoryCollector;

	@Autowired
	DataProcInventoryCollector dataProcInventoryCollector;

	@Autowired
	GKEClusterInventoryCollector gkeClusterInventoryCollector;
	@Autowired
	CloudDNSInventoryCollector cloudDNSInventoryCollector;
	@Autowired
	CloudSqlFilter cloudSqlFilter;
	@Autowired
	NetworkInventoryCollector networkInventoryCollector;

	@Autowired
	ProjectInventoryCollector projectInventoryCollector;

	@Autowired
	ServiceAccountInventoryCollector serviceAccountInventoryCollector;
	@Autowired
	IAMUserCollector iamUserCollector;

	@Autowired
	LoadBalancerCollector loadBalancerCollector;

	@Autowired
	APIKeysInventoryCollector apiKeysInventoryCollector;

	/*@Autowired
	CloudFunctionCollector cloudFunctionCollector;
	@Autowired
	CloudFunctionGen1Collector cloudFunctionGen1Collector;*/

	public void generateFiles(List<ProjectVH> projects, String filePath) {

		try {
			FileManager.initialise(filePath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		for (ProjectVH project : projects) {
			log.info("Started Discovery for project {}", project);

			ExecutorService executor = Executors.newCachedThreadPool();

			/*executor.execute(() -> {
				if (!(isTypeInScope("cloudfunction"))) {
					return;
				}
				try {
					FileManager.generateCloudFunctionFile(cloudFunctionCollector.fetchCloudFunctionInventory(project));
				} catch (Exception e) {
					log.error("Error occured in generating data file for cloud functions {} ", e.getMessage());
				}
			});*/

			executor.execute(() -> {
				if (!(isTypeInScope("computeinstance"))) {
					return;
				}
				try {
					FileManager.generateVMFiles(vmInventoryCollector.fetchInstanceInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("computefirewall"))) {
					return;
				}
				try {
					FileManager.generateFireWallFiles(firewallInventoryCollector.fetchFirewallInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("bigqueydataset"))) {
					log.info("Target type bigqueydataset not found!!. Skipping collector");
					return;
				}
				try {
					log.info("Target type bigqueydataset configured. Executing collector");
					FileManager.generateBigqueryFiles(bigQueryInventoryCollector.fetchBigqueryInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("bigqueytable"))) {
					log.info("Target type bigqueytable not found!!. Skipping collector");
					return;
				}
				try {
					log.info("Target type bigqueytable configured. Executing collector");
					FileManager.generateBigqueryTableFiles(
							bigQueryInventoryCollector.fetchBigqueryTableInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("computestorage"))) {
					return;
				}
				try {
					FileManager.generateStorageFiles(storageInventoryCollector.fetchStorageInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("pubsub"))) {
					return;
				}
				try {
					FileManager.generatePubSubFiles(pubSubInventoryCollector.fetchPubSubInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("cloudsql"))) {
					return;
				}
				try {
					List<CloudSqlVH>cloudSqlVHList=cloudSqlInventoryCollector.fetchCloudSqlInventory(project);
					FileManager.generateCloudSqlFiles(cloudSqlVHList);
					FileManager.generateCloudSqlServerFiles(cloudSqlFilter.filterByDatabaseVersion(cloudSqlVHList, DataBaseTypeEnum.SQLSERVER));
					FileManager.generateCloudMySqlServerFiles(cloudSqlFilter.filterByDatabaseVersion(cloudSqlVHList, DataBaseTypeEnum.MYSQL));
					FileManager.generateCloudPostgresFiles(cloudSqlFilter.filterByDatabaseVersion(cloudSqlVHList, DataBaseTypeEnum.POSTGRES));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("kmskey"))) {
					return;
				}
				try {
					FileManager.generateKmsKeyFiles(kmsKeyInventoryCollector.fetchKmsKeysInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("dataproc"))) {
					return;
				}
				try {
					FileManager.generateDataProcFiles(dataProcInventoryCollector.fetchDataProcInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("gkecluster"))) {
					return;
				}
				try {
					FileManager.generateGKEClusterFiles(gkeClusterInventoryCollector.fetchGKEClusterInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("clouddns"))) {
					return;
				}
				try {
					FileManager.generateCloudDnsFiles(cloudDNSInventoryCollector.fetchCloudDnsInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("networks"))) {
					return;
				}
				try {
					FileManager.generateNetworksFiles(networkInventoryCollector.fetchNetworkInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("project"))) {
					return;
				}
				try {
					FileManager.generateProjectFiles(projectInventoryCollector.fetchProjectMetadataMetadata(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("serviceaccounts"))) {
					return;
				}
				try {
					FileManager.generateServiceAccountFiles(serviceAccountInventoryCollector.fetchServiceAccountDetails(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {

				if (!(isTypeInScope("apikeys"))) {
					return;
				}
				try {
					FileManager.generateApiKeysFiles(apiKeysInventoryCollector.fetchApiKeys(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			executor.execute(() -> {
				if (!(isTypeInScope("iamusers"))) {
					return;
				}
				try {
					FileManager.generateIamUsers(iamUserCollector.fetchIamUsers(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			executor.execute(() -> {
				if (!(isTypeInScope("loadbalancer"))) {
					return;
				}
				try {
					FileManager.generateLoadBalancerFiles(loadBalancerCollector.fetchLoadBalancerInventory(project));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			/*executor.execute(() -> {
				if (!(isTypeInScope("cloudfunctiongen1"))) {
					return;
				}
				try {
					FileManager.generateCloudFunctionGen1File(cloudFunctionGen1Collector.fetchCloudFunctionInventory(project));
				} catch (Exception e) {
					log.error("Error occured in generating data file for cloud functions gen1 {} ", e.getMessage());
				}
			});*/


			executor.shutdown();

			while (!executor.isTerminated()) {
			}

			log.debug("Finished Discovery for sub {}", project);
		}

		try {
			FileManager.finalise();
		} catch (IOException e) {
		}
	}

	private boolean isTypeInScope(String type) {
		if ("".equals(targetTypes)) {
			return true;
		} else {
			List<String> targetTypesList = Arrays.asList(targetTypes.split(","));
			return targetTypesList.contains(type);
		}
	}
}