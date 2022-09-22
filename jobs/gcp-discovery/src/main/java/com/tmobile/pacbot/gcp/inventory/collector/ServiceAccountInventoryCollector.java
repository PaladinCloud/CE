package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.api.services.iam.v1.model.ServiceAccountKey;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import com.tmobile.pacbot.gcp.inventory.vo.ServiceAccountKeyVH;
import com.tmobile.pacbot.gcp.inventory.vo.ServiceAccountVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ServiceAccountInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    private static final Logger logger = LoggerFactory.getLogger(StorageCollector.class);

    public  List<ServiceAccountVH> fetchServiceAccountDetails(ProjectVH project) throws GeneralSecurityException, IOException {

       Iam.Projects.ServiceAccounts.List serviceAccountList= gcpCredentialsProvider.getIamService().projects().serviceAccounts().list("projects/"+project.getProjectId());
        List<ServiceAccountVH> serviceAccountVHList = new ArrayList<>();
       for (ServiceAccount serviceAccount:serviceAccountList.execute().getAccounts()){

           ServiceAccountVH serviceAccountVH=new ServiceAccountVH();
           serviceAccountVH.setProjectName(project.getProjectName());
           serviceAccountVH.setProjectId(project.getProjectId());
           serviceAccountVH.setId(serviceAccount.getUniqueId());
           serviceAccountVH.setName(serviceAccount.getName());
           serviceAccountVH.setEmail(serviceAccount.getEmail());
           serviceAccountVH.setDisplayName(serviceAccount.getDisplayName());
           serviceAccountVH.setDescription(serviceAccount.getDescription());
           logger.info("Serice account inventory collector-> {}",serviceAccount.getName());
           getServiceAccountKeys(serviceAccount.getUniqueId(),serviceAccountVH,project);

           serviceAccountVHList.add(serviceAccountVH);
       }
    return serviceAccountVHList;
    }
    public void  getServiceAccountKeys(String serviceAccountName,ServiceAccountVH serviceAccountVH,ProjectVH projectVH) throws GeneralSecurityException, IOException {

        String apiUrlTemplate = "projects/"+projectVH.getProjectId()+"/serviceAccounts/"+serviceAccountName;

        logger.info("key list type -->{}",apiUrlTemplate);
        Iam.Projects.ServiceAccounts.Keys.List serviceAccountKeyList= gcpCredentialsProvider.getIamService().projects().serviceAccounts().keys().list( apiUrlTemplate);
        List<ServiceAccountKeyVH> serviceAccountKeyVHList= new ArrayList<>();

        for (ServiceAccountKey serviceAccountKey:serviceAccountKeyList.execute().getKeys()) {
            ServiceAccountKeyVH serviceAccountKeyVH=new ServiceAccountKeyVH();
            serviceAccountKeyVH.setName(serviceAccountKey.getName());
            serviceAccountKeyVH.setKeyType(serviceAccountKey.getKeyType());
            serviceAccountKeyVHList.add(serviceAccountKeyVH);
        }
        serviceAccountVH.setServiceAccountKey(serviceAccountKeyVHList);
    }
}
