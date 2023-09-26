package com.tmobile.cso.pacman.datashipper;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import com.tmobile.cso.pacman.datashipper.auth.AWSCredentialProvider;
import com.tmobile.cso.pacman.datashipper.entity.*;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.cso.pacman.datashipper.util.ErrorManageUtil;
import com.tmobile.pacman.commons.jobs.PacmanJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class Main.
 */
@PacmanJob(methodToexecute = "shipData", jobName = "Redshfit-ES-Datashipper", desc = "Job to load data from Redshfit to ES", priority = 5)
public class Main implements Constants {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static String dataSource = null;
    private static String srcFolder = null;

    private static String srcInventoryFolderName = null;

    @Value("${base.account}")
    private static String account;

    /**
     * The account.
     */
    @Value("${s3.role}")
    private static String s3Role;

    @Value("${base.region}")
    private static String region;

    @Value("${s3}")
    private static String s3Bucket;

    @Value("${s3.data}")
    private static String s3Data;

    @Value("${s3.processed}")
    private static String s3Processed;

    static Map<String,String> shipperBackUpAndCleanUpErrorMap = new HashMap<>();
    /**
     * The main method.
     * This will be only used for local testing purpose. This method will never get invoked at deployed environments
     * like saasdev(installer) mode
     * @param args the arguments
     */
    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        Arrays.asList(args).stream().forEach(obj -> {
            String[] paramArray = obj.split("[:]");
            params.put(paramArray[0], paramArray[1]);
        });
        try {
            LOGGER.info("shipData() method is going to be executed");
            shipData(params);
            LOGGER.info("shipData() method is executed sucessfully");
        }
        catch (AmazonS3Exception s3Exception){
            LOGGER.error("s3Exception Occured while Shipping the data", s3Exception.getMessage());
        }
        catch (Exception exception){
            LOGGER.error("exception Occured while Shipping the data", exception.getMessage());
        }

        System.exit(0);
    }

    /**
     * Ship data.
     * This method will be executed as When Shipper jar is getting executed as part of
     * Batch job(i.e) data-shipper-gcp-job,data-shipper-azure-job
     * @param params the params
     * @return
     */
    public static Map<String, Object> shipData(Map<String, String> params) {
        LOGGER.info("Inside shipData Method");
        LOGGER.debug("Shipper Job Params:{}", params);
        String jobName = System.getProperty("jobName");
        List<Map<String, String>> errorList = new ArrayList<>();
        try {
            MainUtil.setup(params);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Exception in setting up Job ");
            errorMap.put(ERROR_TYPE, WARN);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
            return ErrorManageUtil.formErrorCode(jobName, errorList);
        }

       String ds = params.get("datasource");
        try {
            AWSCredentialProvider awsCredentialProvider = new AWSCredentialProvider();
            dataSource = params.get("datasource");
            LOGGER.debug("dataSource:{}", dataSource);
            srcFolder = params.get("s3.data");
            LOGGER.debug("srcFolder:{}", srcFolder);
            ESManager.configureIndexAndTypes(ds, errorList);
            errorList.addAll(new EntityManager().uploadEntityData(ds, srcFolder));
            new ExternalPolicies().uploadPolicyDefinition(ds);
            errorList.addAll(new AssetGroupStatsCollector().collectAssetGroupStats());
            errorList.addAll(new IssueCountManager().populateViolationsCount());
            errorList.addAll(new AssetsCountManager().populateAssetCount());

        } catch (AmazonS3Exception s3Exception) {
            LOGGER.error("Exception Occured inside shipData method while doing Backup and Clean Up Inventory", s3Exception);
            //Adding to error Map which will be part of error list for SHipper Batch Job Processing
            shipperBackUpAndCleanUpErrorMap.put(EXCEPTION,String.valueOf(s3Exception));
        } catch (Exception exception) {
            LOGGER.error("Exception Occured inside shipData method while doing Backup and Clean Up Inventory", exception);
            //Adding to error Map which will be part of error list for SHipper Batch Job Processing
            shipperBackUpAndCleanUpErrorMap.put(EXCEPTION,String.valueOf(exception));
        }

        //add shipperBackUpAndCleanUpErrorMap to errorList collection
        errorList.add(shipperBackUpAndCleanUpErrorMap);

        Map<String, Object> status = ErrorManageUtil.formErrorCode(jobName, errorList);
        LOGGER.info("Job Return Status {} ", status);

        return status;
    }

    /**
     * This Method is used to Back up the Shipped files by Shipper Module and then Clean them up post backing up.
     * datasource (i.e) gcp,azure
     * srcFolder - gcp-inventory,azure-inventory
     *
     * @param dataSource
     * @param srcFolder
     * @param awsCredentialProvider
     */
    public static void doBackUpAndCleanUpInventory(String dataSource, String srcFolder, AWSCredentialProvider awsCredentialProvider) {
        LOGGER.info("Inside doBackUpAndCleanUpInventory Method:{}");

        try {
            //Get the required params from System properties
            account = System.getProperty("base.account");
            region  = System.getProperty("base.region");
            s3Role  = System.getProperty("s3.role");
            s3Processed = System.getProperty("s3.processed");
            s3Bucket    = System.getProperty("s3");
            LOGGER.debug("base account: {}", account);
            LOGGER.debug("base region: {}", region);
            BasicSessionCredentials credentials = awsCredentialProvider.getCredentials(account, region, s3Role);

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

            String backupFolderName = dataSource + "-" + s3Processed;
            //back up the shipped files
            LOGGER.info("Start : Backup Current Files as Part of Shipper Job");
            LOGGER.debug("Printing s3Bucket:{}", s3Bucket);
            LOGGER.info("Printing backupFolderName {}", backupFolderName);
            LOGGER.info("Calling  copytoBackUp Method");
            copytoBackUp(s3Client, s3Bucket, srcFolder, backupFolderName + "/" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
            LOGGER.info("End : Backup Current Files as Part of Shipper Job");

            //clean up the shipped files
            LOGGER.info("Start : Cleaning Up Source Inventory  as Part of Shipper Job");
            deleteFiles(s3Client, s3Bucket, srcFolder);
            LOGGER.info("End : Cleaning Up Source Inventory  as Part of Shipper Job");
        } catch (AmazonS3Exception s3Exception) {
            LOGGER.error("Exception Occurred inside doBackUpAndCleanUpInventory method execution", s3Exception);
            shipperBackUpAndCleanUpErrorMap.put(EXCEPTION,String.valueOf(s3Exception));
        } catch (Exception exception) {
            LOGGER.error("Exception Occurred inside doBackUpAndCleanUpInventory method execution", exception);
            shipperBackUpAndCleanUpErrorMap.put(EXCEPTION,String.valueOf(exception));
        }
    }

    /**
     * This Method is used to Copy the shipped files to backup folder
     * from - azure-inventory,gcp-inventory
     * to   - azure-backup
     */
    private static void copytoBackUp(AmazonS3 s3client, String s3Bucket, String from, String to) {
        LOGGER.info("Inside copyBackUp Method:{}",s3client);
        LOGGER.info("Printing  s3Bucket:{}",s3Bucket);
        LOGGER.info("Printing  from:{}",from);
        LOGGER.info("Printing  to:{}",to);
        String[] keys = listKeys(s3client, s3Bucket, from);
        String fileName = "";
        for (String key : keys) {
            try {
                fileName = key.substring(key.lastIndexOf('/') + 1);
                s3client.copyObject(s3Bucket, key, s3Bucket, to + "/" + fileName);
            } catch (Exception e) {
                LOGGER.info("    Copy " + fileName + "failed", e);
            }
        }
        LOGGER.info("copytoBackUp Method is Done ");
    }

    /**
     * Delete files.
     *
     * @param s3client the s3 client
     * @param s3Bucket the s3 bucket
     * @param folder   the folder
     */
    private static void deleteFiles(AmazonS3 s3client, String s3Bucket, String folder) {
        LOGGER.info("Inside deleteFiles Method");
        LOGGER.info("Printing s3Bucket {}",s3Bucket);
        LOGGER.info("Printing folder {}",folder);
        String[] keys = listKeys(s3client, s3Bucket, folder);
        if (keys != null && keys.length > 0) {
            DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(s3Bucket).withKeys((keys));

            try {
                DeleteObjectsResult result = s3client.deleteObjects(multiObjectDeleteRequest);
                LOGGER.debug("Files Deleted " + result.getDeletedObjects().stream().map(obj -> obj.getKey()).collect(Collectors.toList()));
            } catch (Exception e) {
                LOGGER.error("Delete Failed", e);
            }
        }
        LOGGER.info("deleteFiles Method execution is done successfully");

    }

    /**
     * This method is used to list all files(keys) in specified Bucket
     *
     * @param s3client
     * @param s3Bucket
     * @param folder
     * @return
     */
    private static String[] listKeys(AmazonS3 s3client, String s3Bucket, String folder) {
        LOGGER.info("Inside listKeys Method");
        LOGGER.info("Printing s3client {}",s3client);
        LOGGER.info("Printing s3Bucket {}",s3Bucket);
        LOGGER.info("Printing folder {}",folder);
        try {
            return s3client.listObjectsV2(new ListObjectsV2Request().withBucketName(s3Bucket).withPrefix(folder)).getObjectSummaries().stream().map(S3ObjectSummary::getKey).toArray(String[]::new);
        } catch (Exception e) {
            LOGGER.error("Error in listKeys", e);
        }
        LOGGER.info(" listKeys Method executed Successfully");
        return new String[0];
    }
}