package com.tmobile.cso.pacman.datashipper;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import com.tmobile.cso.pacman.datashipper.auth.AWSCredentialProvider;
import com.tmobile.cso.pacman.datashipper.entity.AssetGroupStatsCollector;
import com.tmobile.cso.pacman.datashipper.entity.AssetsCountManager;
import com.tmobile.cso.pacman.datashipper.entity.EntityManager;
import com.tmobile.cso.pacman.datashipper.entity.IssueCountManager;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.cso.pacman.datashipper.util.ErrorManageUtil;
import com.tmobile.pacman.commons.jobs.PacmanJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.ComponentScan;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class Main.
 */
@PacmanJob(methodToexecute = "shipData", jobName = "Redshfit-ES-Datashipper", desc = "Job to load data from Redshfit to ES", priority = 5)
@ComponentScan
public class Main implements Constants {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static String dataSource = null;
    private static String srcFolder = null;
    // private static String accessKey = null;
    //private static String secretKey = null;
    private static String s3BucketName = null;

    private static String srcInventoryFolderName = null;

    @Value("${base.account}")
    private static String account;

    /** The account. */
    @Value("${s3.role}")
    private static String s3Role;

    @Value("${base.region}")
    private static String region ;

    @Value("${s3}")
    private static String s3Bucket ;

    @Value("${s3.data}")
    private static String s3Data;

    @Value("${s3.processed}")
    private static String s3Processed ;

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        Arrays.asList(args).stream().forEach(obj -> {
            String[] paramArray = obj.split("[:]");
            params.put(paramArray[0], paramArray[1]);
        });
         shipData(params);
        //As part of new Plugin Development , backup files will be handled by Shipper Batch Job.Hence Collector responsibility lies only with Collecting Data.
        try {
            AWSCredentialProvider awsCredentialProvider= new AWSCredentialProvider();
            dataSource = params.get("datasource");
            srcFolder = params.get("s3.data");
            doBackUpAndCleanUpInventory(dataSource, srcFolder,awsCredentialProvider);
        } catch (AmazonS3Exception s3Exception) {
            LOGGER.error("Exception Occured while doing Backup and Clean Up Inventory", s3Exception.getMessage());
        } catch (Exception exception) {
            LOGGER.error("Exception Occured while doing Backup and Clean Up Inventory", exception.getMessage());
        }
        System.exit(0);
    }
    /**
     * Ship data.
     *
     * @param params the params
     * @return
     */
    public static Map<String, Object> shipData(Map<String, String> params) {
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
        ESManager.configureIndexAndTypes(ds, errorList);
        errorList.addAll(new EntityManager().uploadEntityData(ds));
        errorList.addAll(new AssetGroupStatsCollector().collectAssetGroupStats());
        errorList.addAll(new IssueCountManager().populateViolationsCount());
        errorList.addAll(new AssetsCountManager().populateAssetCount());
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
        //accessKey = System.getenv("ACCESS_KEY");
        //secretKey = System.getenv("SECRET_KEY");
        LOGGER.info("Inside doBackUpAndCleanUpInventory Method:{}");
        LOGGER.debug(dataSource);
        LOGGER.debug(srcFolder);

        try {

            BasicSessionCredentials credentials = awsCredentialProvider.getCredentials(account, region, s3Role);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

            srcInventoryFolderName = srcFolder;
            String backupFolderName = dataSource + "-" + s3Processed;
            //back up the shipped files
            LOGGER.info("Start : Backup Current Files as Part of Shipper Job");
            copytoBackUp(s3Client, s3Bucket, srcInventoryFolderName, backupFolderName + "/" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
            //copytoBackUp(s3Client, s3BucketName, srcInventoryFolderName, dataSource + "-backup" + "/" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
            LOGGER.info("End : Backup Current Files as Part of Shipper Job");
            //clean up the shipped files
            LOGGER.info("Start : Cleaning Up Source Inventory  as Part of Shipper Job", srcInventoryFolderName);
            deleteFiles(s3Client, s3Bucket, srcInventoryFolderName);
            LOGGER.info("End : Cleaning Up Source Inventory  as Part of Shipper Job", srcInventoryFolderName);
        } catch (AmazonS3Exception s3Exception) {
            LOGGER.error("Exception Occured while doing Backup and Clean Up Inventory", s3Exception.getMessage());
        } catch (Exception exception) {
            LOGGER.error("Exception Occured while doing Backup and Clean Up Inventory", exception.getMessage());

        }
    }
    /**
     * This Method is used to Copy the shipped files to backup folder
     * from - azure-inventory,gcp-inventory
     * to   - azure-backup
     */
    private static void copytoBackUp(AmazonS3 s3client, String s3Bucket, String from, String to) {
        String[] keys = listKeys(s3client, s3Bucket, from);
        String fileName = "";
        for (String key : keys) {
            try {
                fileName = key.substring(key.lastIndexOf('/') + 1);
                s3client.copyObject(s3Bucket, key, s3Bucket, to + "/" + fileName);
                LOGGER.debug("    Copy " + fileName + " to backup folder");
            } catch (Exception e) {
                LOGGER.info("    Copy " + fileName + "failed", e);
            }
        }
    }

    /**
     * Delete files.
     *
     * @param s3client the s3 client
     * @param s3Bucket the s3 bucket
     * @param folder   the folder
     */
    private static void deleteFiles(AmazonS3 s3client, String s3Bucket, String folder) {

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

    }

    /**
     * This method is used to list all files(keys) in specified Bucket
     * @param s3client
     * @param s3Bucket
     * @param folder
     * @return
     */
    private static String[] listKeys(AmazonS3 s3client, String s3Bucket, String folder) {
        try {
            return s3client.listObjectsV2(new ListObjectsV2Request().withBucketName(s3Bucket).withPrefix(folder)).getObjectSummaries().stream().map(S3ObjectSummary::getKey).toArray(String[]::new);
        } catch (Exception e) {
            LOGGER.error("Error in listKeys", e);
        }
        return new String[0];
    }



}