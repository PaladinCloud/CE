package com.paladincloud.jobscheduler;

import com.paladincloud.jobscheduler.config.ConfigUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobSchedulerApplication {

    public static void main(String[] args) {

        // load the properties from the config server
        try {
            ConfigUtil.setConfigProperties();

        } catch (Exception e) {
            System.err.println("Error fetching config" + e);
//            ErrorManageUtil.uploadError("all", "all", "all", "Error fetching config " + e.getMessage());
            //return ErrorManageUtil.formErrorCode();
        }
        SpringApplication.run(JobSchedulerApplication.class, args);
    }
}
