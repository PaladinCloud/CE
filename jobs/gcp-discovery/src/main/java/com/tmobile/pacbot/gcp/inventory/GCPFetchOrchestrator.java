package com.tmobile.pacbot.gcp.inventory;

import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.file.AssetFileGenerator;
import com.tmobile.pacbot.gcp.inventory.file.S3Uploader;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

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
            log.info("Before Fetching projects!!");
            List<ProjectVH> allProjects = fetchProjects();
            log.info("After fetching projects");
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

    private List<ProjectVH> fetchProjects() {

        List<String> projectList = new ArrayList<>();
        List<ProjectVH> projectDetails=new ArrayList<>();
        log.info("projects before: {} ", projects);
            if (projects != null && !"".equals(projects)) {
            projectList = Arrays.asList(projects.split(","));
        }

            log.debug("Project post splitting: {} ", String.join(", ", projectList));
        try {
            log.info("Entering getCloudResourceManager!!");
            CloudResourceManager resource = gcpCredentialsProvider.getCloudResourceManager();
            for(String projectId:projectList){
                log.info("Entering the loop!!");
                CloudResourceManager.Projects.Get project = resource.projects().get(projectId);
                log.info("Before execute");

                log.info("Value of the project is: {}", project.toString());
                log.info("Value of the project is: {}", project.getHttpContent());
                try{
                    Project p = project.execute();
                    log.info("After Execute!!");
                    log.info("Project Id: {}, Project Name: {}, Project number: {}"
                            ,p.getProjectId(),p.getName(),p.getProjectNumber());
                    ProjectVH projectVH=new ProjectVH();
                    projectVH.setProjectId(p.getProjectId());
                    projectVH.setProjectName(p.getName());
                    projectVH.setProjectNumber(p.getProjectNumber());
                    projectDetails.add(projectVH);
                } catch (Exception e){
                    log.info("Exception while executing the project: {}" , e.getMessage());
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Total projects in Scope : {}", projectDetails.size());
        log.info("Projects : {}", projectDetails);
        return projectDetails;
    }
}
