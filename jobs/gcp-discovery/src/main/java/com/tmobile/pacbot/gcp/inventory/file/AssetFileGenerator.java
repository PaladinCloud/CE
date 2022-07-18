package com.tmobile.pacbot.gcp.inventory.file;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.collector.*;
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

	public void generateFiles(List<String> projects, String filePath) {

		try {
			FileManager.initialise(filePath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// generateAzureAplicationList();

		for (String project : projects) {
			log.info("Started Discovery for project {}", project);

			ExecutorService executor = Executors.newCachedThreadPool();

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
					FileManager.generateBigqueryTableFiles(bigQueryInventoryCollector.fetchBigqueryTableInventory(project));
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
					FileManager.generateCloudSqlFiles(cloudSqlInventoryCollector.fetchCloudSqlInventory(project));
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

	// /**
	// * function for generating registered application file
	// */
	// private void generateAzureAplicationList() {
	//
	// if ((isTypeInScope("registeredApplication"))) {
	// try {
	// FileManager.generateRegisteredApplicationFiles(
	// registeredApplicationInventoryCollector.fetchAzureRegisteredApplication());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }

	private boolean isTypeInScope(String type) {
		if ("".equals(targetTypes)) {
			return true;
		} else {
			List<String> targetTypesList = Arrays.asList(targetTypes.split(","));
			return targetTypesList.contains(type);
		}
	}
}
