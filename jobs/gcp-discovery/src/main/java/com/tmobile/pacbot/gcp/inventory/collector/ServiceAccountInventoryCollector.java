package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.GetIamPolicyRequest;
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
import java.security.GeneralSecurityException;
import java.util.*;

@Component
public class ServiceAccountInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    private static final Logger logger = LoggerFactory.getLogger(ServiceAccountInventoryCollector.class);

    public  List<ServiceAccountVH> fetchServiceAccountDetails(ProjectVH project) throws GeneralSecurityException, IOException {

       Iam.Projects.ServiceAccounts.List serviceAccountList= gcpCredentialsProvider.getIamService().projects().serviceAccounts().list("projects/"+project.getProjectId());
        List<ServiceAccountVH> serviceAccountVHList = new ArrayList<>();

        Map<String,ServiceAccountVH> serviceAccountMap = new HashMap<>();
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
           serviceAccountMap.put(serviceAccount.getName(), serviceAccountVH);

           //Method to fetch the role and membership of accounts of project
           fetchMembership(serviceAccountVH,project.getProjectId());

           serviceAccountVHList.add(serviceAccountVH);

       }
       return serviceAccountVHList;
    }
    private void fetchMembership(ServiceAccountVH serviceAccountVH, String projectId){
        CloudResourceManager.Projects.GetIamPolicy iamPolicy = null;
        String serviceAccounts="serviceAccounts/";
        String serviceAccount="serviceAccount:";
        try {
           List<String> roles=new ArrayList<>();
            String name=serviceAccountVH.getName();
            String modifiedName=name.substring(name.indexOf(serviceAccounts)+serviceAccounts.length());
            iamPolicy = gcpCredentialsProvider.getCloudResourceManager().projects().getIamPolicy(projectId, new GetIamPolicyRequest());
            List<com.google.api.services.cloudresourcemanager.model.Binding> binds = iamPolicy.execute().getBindings();
            if(binds!=null) {
                for (com.google.api.services.cloudresourcemanager.model.Binding binding : binds) {
                    for(String member:binding.getMembers())
                    {
                        if(member.startsWith(serviceAccount))
                        {
                            String expectedName=serviceAccount.concat(modifiedName);
                            if(expectedName.equals(member))
                            {
                                roles.add(binding.getRole());
                            }

                        }
                    }
                }
            }
            serviceAccountVH.setRoles(roles);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
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
