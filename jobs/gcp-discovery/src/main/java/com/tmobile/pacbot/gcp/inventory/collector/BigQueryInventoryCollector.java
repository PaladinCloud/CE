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
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class BigQueryInventoryCollector {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:00:00Z";
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(BigQueryInventoryCollector.class);

    public List<BigQueryVH> fetchBigqueryInventory(ProjectVH project) throws IOException {
        logger.info("Running collector for bigquery inventory.");
        List<BigQueryVH> bigQueryList = new ArrayList<>();

        BigQueryOptions bigQueryOption = gcpCredentialsProvider.getBigQueryOptions()
                .setProjectId(project.getProjectId()).build();

        BigQuery bigQuery = bigQueryOption.getService();

        Page<Dataset> dataSetList = bigQuery.listDatasets(project.getProjectId(), BigQuery.DatasetListOption.all());

        try {
            for (Dataset dataSet : dataSetList.iterateAll()) {

                BigQueryVH bigQueryVH = new BigQueryVH();
                bigQueryVH.setId(dataSet.getDatasetId().getDataset());
                bigQueryVH.setProjectName(project.getProjectName());
                bigQueryVH.setDatasetId(dataSet.getDatasetId().getDataset());
                bigQueryVH.setProjectId(project.getProjectId());
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

    public List<BigQueryTableVH> fetchBigqueryTableInventory(ProjectVH project) throws IOException {
        logger.info("Running collector for bigquery table inventory. Project Name:{}", project.getProjectName());
        BigQueryOptions bigQueryOption = gcpCredentialsProvider.getBigQueryOptions()
                .setProjectId(project.getProjectId()).build();

        BigQuery bigQuery = bigQueryOption.getService();

        Page<Dataset> dataSetList = bigQuery.listDatasets(project.getProjectId(), BigQuery.DatasetListOption.all());
        List<BigQueryTableVH> tableVHList = new ArrayList<>();
        for (Dataset dataSet : dataSetList.iterateAll()) {
            String datasetId = dataSet.getDatasetId().getDataset();
            logger.debug("Dataset found. Dataset id:{} .Extracting table data for dataset.", datasetId);
            String location=getDataSetLocation(dataSet,bigQuery);
            try {
                Page<Table> tableList = dataSet.list(BigQuery.TableListOption.pageSize(100));
                for (Table table : tableList.iterateAll()) {
                    String tableId = table.getTableId().getTable();
                    Table tableData=bigQuery.getTable(TableId.of(datasetId, tableId));
                    logger.info("Table data :{}",tableData);
                    BigQueryTableVH tableVH = new BigQueryTableVH();
                    tableVH.setId(tableId);
                    tableVH.setDataSetId(tableData.getTableId().getDataset());
                    tableVH.setProjectName(project.getProjectName());
                    tableVH.setProjectId(project.getProjectId());
                    tableVH.setTableId(tableId);
                    tableVH.setIamResourceName(tableData.getTableId().getIAMResourceName());
                    tableVH.set_cloudType(InventoryConstants.CLOUD_TYPE_GCP);
                    tableVH.setDescription(tableData.getDescription());
                    tableVH.setFriendlyName(tableData.getFriendlyName());
                    tableVH.setGeneratedId(tableData.getGeneratedId());
                    tableVH.setEtag(tableData.getEtag());
                    tableVH.setExpirationTime(getFormattedDate(tableData.getExpirationTime()));
                    tableVH.setLastModifiedTime(getFormattedDate(tableData.getLastModifiedTime()));
                    tableVH.setCreationTime(getFormattedDate(tableData.getCreationTime()));
                    tableVH.setRegion(location);
                    tableVH.setLabels(tableData.getLabels());
                    if (tableData.getEncryptionConfiguration() != null) {
                        tableVH.setKmsKeyName(tableData.getEncryptionConfiguration().getKmsKeyName());
                    }
                    tableVH.setNumBytes(tableData.getNumBytes());
                    tableVH.setRowNum(tableData.getNumRows());
                    tableVH.setSelfLink(tableData.getSelfLink());
                    tableVHList.add(tableVH);
                }
            } catch (Exception e) {
                logger.error("Error while fetching inventory data for BigQuery :", e);
            }
        }
        logger.info("BigQuery Collected for project:{}, number of tables found:{}", project.getProjectId(), tableVHList.size());
        return tableVHList;
    }

    private String getDataSetLocation(Dataset dataSet, BigQuery bigQuery) {
        if(dataSet.getLocation()!=null){
            return dataSet.getLocation();
        }else{
            Dataset locationData=bigQuery.getDataset(dataSet.getDatasetId(),
                    BigQuery.DatasetOption.fields(BigQuery.DatasetField.LOCATION));
            return locationData.getLocation();
        }
    }

    private String getFormattedDate(Long timeData){
        if(timeData!=null) {
            try {
                return new SimpleDateFormat(DATE_FORMAT).format(new Date(timeData));
            } catch (Exception e) {
                logger.error("Error in parsing date format",e);
                return null;
            }
        }
        return null;
    }
}
