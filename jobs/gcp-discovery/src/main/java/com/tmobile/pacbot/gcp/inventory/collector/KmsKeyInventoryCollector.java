package com.tmobile.pacbot.gcp.inventory.collector;


import com.google.cloud.kms.v1.*;
import com.google.iam.v1.Policy;
import com.tmobile.pacbot.gcp.inventory.InventoryConstants;
import com.tmobile.pacbot.gcp.inventory.util.GCPlocationUtil;
import com.tmobile.pacbot.gcp.inventory.vo.Bindings;
import com.tmobile.pacbot.gcp.inventory.vo.KMSKeyVH;
import com.tmobile.pacman.commons.gcp.clients.GCPCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class KmsKeyInventoryCollector {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    @Autowired
    GCPlocationUtil gcPlocationUtil;

    private static final Logger logger = LoggerFactory.getLogger(KmsKeyInventoryCollector.class);

    public List<KMSKeyVH> fetchKmsKeysInventory(String projectId) throws IOException {
        logger.info("Running collector for cloud KMS keys inventory.");
        List<KMSKeyVH> kmsKeyList = new ArrayList<>();

        try {
            KeyManagementServiceClient kmsKeyClient = gcpCredentialsProvider.getKmsKeyServiceClient();

            List<String>locations=new ArrayList<>(gcPlocationUtil.getLocations(projectId));
            logger.info("Fetched GCP locations: {}", locations);
            for(String loc:locations){
                logger.info("Fetching key rings for location: {}",loc);
                String parentForRings= LocationName.of(projectId, loc).toString();
                logger.info("Parent for fetching key rings : {}",parentForRings);
                KeyManagementServiceClient.ListKeyRingsPagedResponse keyRingsResponse = kmsKeyClient.listKeyRings(parentForRings);
                logger.info("KeyRing fetch response:{}", keyRingsResponse);
                fetchKmsKeys(projectId, kmsKeyList, kmsKeyClient, loc, keyRingsResponse);
            }
        } catch (Exception e) {
            logger.error("Exception in fetching kms key data", e);
        }

        logger.info("KMS key data collected list size: {}", kmsKeyList.size());
        return kmsKeyList;
    }

    private void fetchKmsKeys(String projectId, List<KMSKeyVH> kmsKeyList, KeyManagementServiceClient kmsKeyClient, String loc, KeyManagementServiceClient.ListKeyRingsPagedResponse keyRingsResponse) {
        for(KeyRing keyRing: keyRingsResponse.iterateAll()){
            String key=keyRing.getName();
            String keyRingName =key.substring(key.lastIndexOf("/")+1,key.length());
            logger.info("Fetched Key ring :{}", keyRingName);
            KeyRingName parent = KeyRingName.of(projectId, loc, keyRingName);
            logger.info("Parent for fetching keys : {}",parent);
            KeyManagementServiceClient.ListCryptoKeysPagedResponse listCryptoKeysPagedResponse = kmsKeyClient.listCryptoKeys(parent);
            populateKmsData(projectId, kmsKeyList, kmsKeyClient, loc, keyRingName, listCryptoKeysPagedResponse);
        }
    }

    private void populateKmsData(String projectId, List<KMSKeyVH> kmsKeyList, KeyManagementServiceClient kmsKeyClient, String loc, String keyRingName, KeyManagementServiceClient.ListCryptoKeysPagedResponse listCryptoKeysPagedResponse) {
        logger.info("Crypto list response: {}", listCryptoKeysPagedResponse);
        for (CryptoKey cryptoKey : listCryptoKeysPagedResponse.iterateAll()) {
            KMSKeyVH kmsKeyVH = new KMSKeyVH();
            logger.info("Crypto key details: {}", cryptoKey);
            kmsKeyVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);
            kmsKeyVH.setId(cryptoKey.getName());
            kmsKeyVH.setPurpose(cryptoKey.getPurpose().toString());
            kmsKeyVH.setProjectName(projectId);
            kmsKeyVH.setRegion(loc);
            kmsKeyVH.setKeyRingName(keyRingName);
            kmsKeyVH.setName(cryptoKey.getName());
            kmsKeyVH.setCryptoBackend(cryptoKey.getCryptoKeyBackend());
            kmsKeyVH.setImportOnly(cryptoKey.getImportOnly());
            kmsKeyVH.setLabelsCount(cryptoKey.getLabelsCount());
            kmsKeyVH.setLabels(cryptoKey.getLabelsMap());
            setIamPolicies(kmsKeyVH, kmsKeyClient.getIamPolicy(cryptoKey.getName()));
            kmsKeyList.add(kmsKeyVH);
        }
    }

    private void setIamPolicies(KMSKeyVH kmsKeyVH, Policy iamPolicy) {
        logger.info("Iam policies: {}",iamPolicy);
        if (iamPolicy != null && !CollectionUtils.isEmpty(iamPolicy.getBindingsList())) {
            List<Bindings> bindingList = iamPolicy.getBindingsList().stream()
                    .map(b -> new Bindings(b.getRole(), b.getMembersList())).collect(Collectors.toList());
            kmsKeyVH.setBindings(bindingList);
        }

    }

}