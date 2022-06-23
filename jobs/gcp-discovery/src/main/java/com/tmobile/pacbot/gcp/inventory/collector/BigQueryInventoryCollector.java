package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.*;
import com.tmobile.pacbot.gcp.inventory.InventoryConstants;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

@Component
public class BigQueryInventoryCollector {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(BigQueryInventoryCollector.class);

    public List<BigQueryVH> fetchBigqueryInventory(String projectId) throws IOException {
        logger.info("Running collector for bigquery inventory.");
        List<BigQueryVH> bigQueryList = new ArrayList<>();

        BigQueryOptions bigQueryOption = gcpCredentialsProvider.getBigQueryOptions()
                .setProjectId(projectId).build();

        BigQuery bigQuery = bigQueryOption.getService();

        Page<Dataset> dataSetList = bigQuery.listDatasets(projectId, BigQuery.DatasetListOption.all());

        try {
            for (Dataset dataSet : dataSetList.iterateAll()) {

                BigQueryVH bigQueryVH = new BigQueryVH();
                bigQueryVH.setId(dataSet.getDatasetId().getDataset());
                bigQueryVH.setProjectName(projectId);
                bigQueryVH.setDatasetId(dataSet.getDatasetId().getDataset());
                bigQueryVH.setProjectId(dataSet.getDatasetId().getProject());
                bigQueryVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);
                logger.info("Populating dataset data for DataSetId: {}, ProjectId: {}",dataSet.getDatasetId().getDataset(),dataSet.getDatasetId().getProject());

                Dataset detailedDataset=bigQuery.getDataset(dataSet.getDatasetId(), BigQuery.DatasetOption.fields(BigQuery.DatasetField.ACCESS,
                        BigQuery.DatasetField.LOCATION, BigQuery.DatasetField.DESCRIPTION, BigQuery.DatasetField.FRIENDLY_NAME,
                        BigQuery.DatasetField.ETAG));
                logger.info("Access list:{}",detailedDataset.getAcl());

                bigQueryVH.setRegion(dataSet.getLocation()!=null?dataSet.getLocation():detailedDataset.getLocation());
                bigQueryVH.setDescription(dataSet.getDescription()!=null?dataSet.getDescription():detailedDataset.getDescription());
                bigQueryVH.setEtag(dataSet.getEtag()!=null?dataSet.getEtag():detailedDataset.getEtag());
                bigQueryVH.setFriendlyName(dataSet.getFriendlyName()!=null?dataSet.getFriendlyName():detailedDataset.getFriendlyName());

                bigQueryVH.setDefaultTableLifetime(dataSet.getDefaultTableLifetime());
                bigQueryVH.setGeneratedId(dataSet.getGeneratedId());
                bigQueryVH.setLastModified(dataSet.getLastModified());
                bigQueryVH.setLabels(dataSet.getLabels());
                bigQueryVH.setDefaultPartitionExpirationMs(dataSet.getDefaultPartitionExpirationMs());

                List<Acl> acls = detailedDataset.getAcl();
                bigQueryVH.setAcl(getAclMap(acls));

                if(dataSet.getDefaultEncryptionConfiguration()!=null) {
                    bigQueryVH.setKmsKeyName(dataSet.getDefaultEncryptionConfiguration().getKmsKeyName());
                }
                bigQueryList.add(bigQueryVH);
            }
        } catch (Exception e) {
            logger.error("Error while fetching inventory data for BigQuery ", e);
        }
        logger.info("BigQuery Collected list size: {}",bigQueryList.size());
        return bigQueryList;
    }

    private List<Map<String,String>> getAclMap(List<Acl> aclList){
        logger.info("Converting the Access control data");
        List<Map<String,String>> result=new ArrayList<>();
        if(!CollectionUtils.isEmpty(aclList)){
            for (Acl acl : aclList) {
                String entity = acl.toString();
                logger.info("ACL: entity: {}", entity);
                int startIndex=entity.indexOf("{",entity.indexOf("{")+1)+1;
                int endIndex=entity.indexOf("}");
                String data=entity.substring(startIndex, endIndex);
                if(data.contains(",") && data.split(",").length>1) {
                    String keyData = data.split(",")[0];
                    String valueData = data.split(",")[1];
                    result.add(getACLEntry(keyData,valueData));
                }
            }
            logger.info("ACL Map: {}", result);
        }else{
            logger.info("ACL list passed is empty");
        }
        logger.info("Map created with size:{}",result.size());
        return result;
    }

    private Map<String,String> getACLEntry(String keyData,String valueData){
        Map<String,String> aclMap=new HashMap<>();
        if(keyData.contains("=")){
            String key=keyData.split("=")[0].replaceAll("\\s","").trim();
            String value=keyData.split("=")[1].replaceAll("\\s","").trim();
            aclMap.put(key,value);
        }
        if(valueData.contains("=")){
            String key=valueData.split("=")[0].replaceAll("\\s","").trim();
            String value=valueData.split("=")[1].replaceAll("\\s","").trim();
            aclMap.put(key,value);
        }
        return aclMap;
    }
}
