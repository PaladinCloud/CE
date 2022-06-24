package com.tmobile.pacbot.gcp.inventory;

import java.text.SimpleDateFormat;
import java.util.*;

import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tmobile.pacbot.gcp.inventory.file.AssetFileGenerator;
import com.tmobile.pacbot.gcp.inventory.file.S3Uploader;

@Component
public class GCPFetchOrchestrator {

    @Autowired
    AssetFileGenerator fileGenerator;

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    /**
     * The s 3 uploader.
     */
    @Autowired
    S3Uploader s3Uploader;

    @Value("${file.path}")
    private String filePath;

    @Value("${project_ids:}")
    private String projects;

    @Value("${s3}")
    private String s3Bucket;

    @Value("${s3.data}")
    private String s3Data;

    @Value("${s3.processed}")
    private String s3Processed;

    @Value("${s3.region}")
    private String s3Region;

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(GCPFetchOrchestrator.class);

    public Map<String, Object> orchestrate() {

        try {
            List<String> allProjects = fetchProjects();
            if (allProjects.isEmpty()) {
                ErrorManageUtil.uploadError("all", "all", "all", "Error fetching projects Info ");
                return ErrorManageUtil.formErrorCode();
            }

            log.info("Start : FIle Generation");
            fileGenerator.generateFiles(allProjects, filePath);
            log.info("End : FIle Generation");

            log.info("Start : Backup Current Files");
            s3Uploader.backUpFiles(s3Bucket, s3Region, s3Data, s3Processed + "/" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
            log.info("End : Backup Current Files");

            log.info("Start : Upload Files to S3");
            s3Uploader.uploadFiles(s3Bucket, s3Data, s3Region, filePath);
            log.info("End : Upload Files to S3");

        } catch (Exception e) {

        }
        return null;
    }

    private List<String> fetchProjects() {

        List<String> projectList = new ArrayList<>();

        if (projects != null && !"".equals(projects)) {
            projectList = Arrays.asList(projects.split(","));
        }
        log.info("Total projects in Scope : {}", projectList.size());
        log.info("Subscriptions : {}", projectList);
        return projectList;
    }
}
