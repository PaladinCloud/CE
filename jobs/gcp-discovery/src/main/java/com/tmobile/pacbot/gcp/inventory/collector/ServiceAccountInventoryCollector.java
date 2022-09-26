package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.GetIamPolicyRequest;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.model.Binding;
import com.google.api.services.iam.v1.model.Policy;
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
           getServiceAccountMemberRoles(serviceAccount,serviceAccountVH);
           serviceAccountMap.put(serviceAccount.getName(), serviceAccountVH);

           //Method to fetch the role and membership of accounts of project
           fetchMembership(serviceAccountMap,project.getProjectId());

           serviceAccountVHList.add(serviceAccountVH);

       }
       return serviceAccountVHList;
    }
    private void fetchMembership(Map<String, ServiceAccountVH> serviceAccount, String projectId){
        CloudResourceManager.Projects.GetIamPolicy iamPolicy = null;
        try {
            iamPolicy = gcpCredentialsProvider.getCloudResourceManager().projects().getIamPolicy(projectId, new GetIamPolicyRequest());
            List<com.google.api.services.cloudresourcemanager.model.Binding> binds = iamPolicy.execute().getBindings();
            if(binds!=null) {
                for (com.google.api.services.cloudresourcemanager.model.Binding binding : binds) {
                    //TODO: process these bindings
                    logger.info("Binding role: {}",binding.getRole());
                    logger.info("Binding members: {}",binding.getMembers());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    private void getServiceAccountMemberRoles(ServiceAccount serviceAccount, ServiceAccountVH serviceAccountVH)  {
        Iam.Projects.ServiceAccounts.GetIamPolicy requestPolicy =
                null;
        try {
            requestPolicy = gcpCredentialsProvider.getIamService().projects().serviceAccounts().getIamPolicy(serviceAccount.getName());
            Policy responsePolicy = requestPolicy.execute();
            Map<String,List<String>> roleMembers=new HashMap() ;
            List<Binding> bindings=responsePolicy.getBindings();
            if(bindings!=null) {
                for (Binding binding : bindings) {
                    roleMembers.put(binding.getRole(), binding.getMembers());
                }
            }
            serviceAccountVH.setRolesMembers(roleMembers);
        } catch (Exception e) {
            e.printStackTrace();
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
