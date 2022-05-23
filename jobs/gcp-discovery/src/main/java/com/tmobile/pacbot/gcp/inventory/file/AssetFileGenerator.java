package com.tmobile.pacbot.gcp.inventory.file;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.collector.VMInventoryCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AssetFileGenerator {

	@Autowired
	GCPCredentialsProvider gcpCredentialsProvider;
	/** The target types. */
	@Value("${targetTypes:}")
	private String targetTypes;

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(AssetFileGenerator.class);

	@Autowired
    VMInventoryCollector vmInventoryCollector;


	public void generateFiles(List<String> projects, String filePath) {

		try {
			FileManager.initialise(filePath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// generateAzureAplicationList();

		for (String project : projects) {
			log.info("Started Discovery for project {}", project);

			// we need to revisit this once we support multiple orgs in gcp
//			try {
//				String accessToken = gcpCredentialsProvider.getAuthToken(project.getTenant());
//				Azure azure = gcpCredentialsProvider.authenticate(project.getTenant(),project.getSubscriptionId());
//				gcpCredentialsProvider.putClient(project.getTenant(),project.getSubscriptionId(), azure);
//				gcpCredentialsProvider.putToken(project.getTenant(), accessToken);
//
//			} catch (Exception e) {
//				log.error("Error authenticating for {}",project,e);
//				continue;
//			}
		

//			List<ResourceGroupVH> resourceGroupList = new ArrayList<ResourceGroupVH>();
//			try {
//				resourceGroupList = resourceGroupInventoryCollector.fetchResourceGroupDetails(project);
//
//			} catch (Exception e) {
//				e.printStackTrace();
//
//			}
//			Map<String, Map<String, String>> tagMap = resourceGroupList.stream()
//					.collect(Collectors.toMap(x -> x.getResourceGroupName().toLowerCase(), x -> x.getTags()));
//
//			List<PolicyDefinitionVH> policyDefinitionList = policyDefinitionInventoryCollector
//					.fetchPolicyDefinitionDetails(project);

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

//	/**
//	 * function for generating registered application file
//	 */
//	private void generateAzureAplicationList() {
//
//		if ((isTypeInScope("registeredApplication"))) {
//			try {
//				FileManager.generateRegisteredApplicationFiles(
//						registeredApplicationInventoryCollector.fetchAzureRegisteredApplication());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

	private boolean isTypeInScope(String type) {
		if ("".equals(targetTypes)) {
			return true;
		} else {
			List<String> targetTypesList = Arrays.asList(targetTypes.split(","));
			return targetTypesList.contains(type);
		}
	}
}
