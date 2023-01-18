package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.api.apikeys.v2.ApiKeysClient;
import com.google.api.apikeys.v2.ApiTarget;

import com.google.api.apikeys.v2.Key;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.APIKeysVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.*;

@Component
public class APIKeysInventoryCollector {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    private static final Logger logger = LoggerFactory.getLogger(APIKeysInventoryCollector.class);

    public List<APIKeysVH> fetchApiKeys(ProjectVH projectVH) throws Exception {
        String parent = "projects/"+projectVH.getProjectId()+"/locations/global";

      logger.info("services ******** {}",gcpCredentialsProvider.getApiKeysService().listKeys(parent));
     ApiKeysClient.ListKeysPagedResponse apiKeys=  gcpCredentialsProvider.getApiKeysService().listKeys(parent);
       List<APIKeysVH> apiKeysVHList=new ArrayList<>();
       for (Key keys:apiKeys.iterateAll() ){
           logger.info("keys list ******** {}",keys.getDisplayName());

            APIKeysVH apiKeysVH=new APIKeysVH();
            apiKeysVH.setId(keys.getUid());
            apiKeysVH.setName(keys.getName());
            apiKeysVH.setDisplayName(keys.getDisplayName());
            apiKeysVH.setRegion("global");
            apiKeysVH.setProjectId(projectVH.getProjectId());
            apiKeysVH.setProjectName(projectVH.getProjectName());
            HashMap<String, Object> restriction=new HashMap<>();

           if(!keys.getRestrictions().getAllFields().isEmpty()){


               List<String>service=new ArrayList<>();
               List<ApiTarget>apiTargets=keys.getRestrictions().getApiTargetsList();
               for(ApiTarget apiTarget:apiTargets){
                   service.add(apiTarget.getService());
                   logger.info("apiKey{}",apiTarget.getService());
               }
               apiKeysVH.setApiTargetList(service);

               HashMap<String,Object>serverKeyRestrictions=new HashMap<>();

               keys.getRestrictions().getServerKeyRestrictions().getAllFields().forEach((fieldDescriptor, o) -> {
                   serverKeyRestrictions.put(fieldDescriptor.getName(),o);
                   logger.info( " getServerKeyRestrictions {}",o);

               });
               restriction.put("serverKeyRestrictions",serverKeyRestrictions);

               HashMap<String,Object>browserKeyRestrictions=new HashMap<>();

               keys.getRestrictions().getBrowserKeyRestrictions().getAllFields().forEach((fieldDescriptor, o) -> {
                   browserKeyRestrictions.put(fieldDescriptor.getName(),o);
                   logger.info( fieldDescriptor.getName(),o);

               });
               restriction.put("browserKeyRestrictions",browserKeyRestrictions);

               HashMap<String,Object>androidKeyRestrictions=new HashMap<>();

               keys.getRestrictions().getAndroidKeyRestrictions().getAllFields().forEach((fieldDescriptor, o) -> {
                   androidKeyRestrictions.put(fieldDescriptor.getName(),o);
                   logger.info( " androidKeyRestrictions {}",o);

               });
               restriction.put("androidKeyRestrictions",androidKeyRestrictions);

               HashMap<String,Object>iosKeyRestrictions=new HashMap<>();

               keys.getRestrictions().getIosKeyRestrictions().getAllFields().forEach((fieldDescriptor, o) -> {
                   iosKeyRestrictions.put(fieldDescriptor.getName(),o);
                   logger.info( " iosKeyRestrictions {}",o);

               });
               restriction.put("iosKeyRestrictions",iosKeyRestrictions);
           }

           apiKeysVH.setCreatedDate(new Date(keys.getCreateTime().getSeconds()*1000).toString());

            apiKeysVH.setRestrictions(restriction);
            apiKeysVHList.add(apiKeysVH);


       }
        return apiKeysVHList;
    }

    }
