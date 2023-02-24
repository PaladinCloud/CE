package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.GetIamPolicyRequest;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.IAMUserVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IAMUserCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    private static final Logger logger = LoggerFactory.getLogger(IAMUserCollector.class);

    public List<IAMUserVH> fetchIamUsers(ProjectVH project) throws GeneralSecurityException, IOException {
        List<IAMUserVH> iamUserVHS= new ArrayList<>();
        CloudResourceManager.Projects.GetIamPolicy iamPolicy = null;
        iamPolicy = gcpCredentialsProvider.getCloudResourceManager(project.getProjectId()).projects().getIamPolicy(project.getProjectId(), new GetIamPolicyRequest());
        List<Binding> binds = iamPolicy.execute().getBindings();
        if(binds!=null){
            for (com.google.api.services.cloudresourcemanager.model.Binding binding : binds) {
                IAMUserVH iamUserVH;
                        for (String member:binding.getMembers()) {
                            iamUserVH=new IAMUserVH();
                            if(iamUserVHS.stream().filter(e -> e.getUserId().equalsIgnoreCase(member)).count()>0) {
                                iamUserVHS.stream().filter(e -> e.getUserId().equalsIgnoreCase(member)).collect(Collectors.toList()).forEach(user -> {
                                    HashSet<String> roles = user.getRoles();
                                    roles.add(binding.getRole());
                                    user.setRoles(roles);
                                });
                            }
                            else if(member.split(":")[0].equalsIgnoreCase("user")){

                                iamUserVH.setUserId(member);
                                iamUserVH.setEmail(member.split(":")[1]);
                                iamUserVH.setProjectId(project.getProjectId());
                                iamUserVH.setProjectName(project.getProjectName());
                                iamUserVH.setRegion(project.getRegion());
                                iamUserVH.setId(project.getProjectId()+"_"+member.split(":")[1]);
                                HashSet<String> roles = new HashSet<>();
                                roles.add(binding.getRole());
                                iamUserVH.setRoles(roles);
                                iamUserVHS.add(iamUserVH);
                            }

                        }

                    }

            }
        return iamUserVHS;
    }
}
