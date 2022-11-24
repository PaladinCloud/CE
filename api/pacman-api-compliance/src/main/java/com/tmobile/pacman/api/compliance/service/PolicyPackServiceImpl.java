package com.tmobile.pacman.api.compliance.service;

import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.compliance.controller.PolicyPackController;
import com.tmobile.pacman.api.compliance.domain.PolicyPackDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
public class PolicyPackServiceImpl implements PolicyPackService{

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyPackServiceImpl.class);
    @Autowired
    PacmanRdsRepository pacmanRdsRepository;

    @Override
    public List<PolicyPackDetails> getAllPolicyPacks() {
        LOGGER.debug("inside getAllPolicyPacks() method of PolicyPackServiceImpl");
        String policyPackQueryString = "select p1.policyPackId as parentPolicyPackId, p1.policyPackName as parentPolicyPack, p1.policyPackDescription as parentPolicyPackDescription,\n" +
                " p1.topLevel as parentTopLevel, p2.policyPackId as childPolicyPackId, p2.policyPackName as childPolicyPack, p2.policyPackDescription as childPolicyPackDescription, p2.topLevel as childTopLevel" +
                " from cf_PolicyPack p1, cf_PolicyPack p2, cf_PolicyPackInfo pp \n" +
                "where p1.policyPackId=pp.parentPolicyPackId and p2.policyPackId=pp.childPolicyPackId" +
                "" +
                " UNION " +
                "" +
                "select p1.policyPackId ,p1.policyPackName  ,p1.policyPackDescription ,p1.topLevel ,null,null,null,null from cf_PolicyPack p1";
        List<Map<String, Object>> policyPackList = pacmanRdsRepository.getDataFromPacman(policyPackQueryString);
        LOGGER.debug("number of rows obtained from cf_PolicyPackInfo: "+policyPackList.size());
        Map<String,PolicyPackDetails> policyPackDetailsMap=new HashMap<>();
        for(Map<String, Object> map : policyPackList){
            String parentPolicyPackId = (String)map.get("parentPolicyPackId");
            String childPolicyPackId = (String)map.get("childPolicyPackId");
            PolicyPackDetails parentPolicyPackDetails= null;
            PolicyPackDetails childPolicyPackDetails=null;
            if(childPolicyPackId==null){
                if(!policyPackDetailsMap.keySet().contains(parentPolicyPackId)) {
                    parentPolicyPackDetails= new PolicyPackDetails();
                    parentPolicyPackDetails.setId(parentPolicyPackId);
                    parentPolicyPackDetails.setName((String)map.get("parentPolicyPack"));
                    parentPolicyPackDetails.setDescription((String)map.get("parentPolicyPackDescription"));
                    parentPolicyPackDetails.setTopLevel((Integer)map.get("parentTopLevel"));
                    policyPackDetailsMap.put(parentPolicyPackId,parentPolicyPackDetails);
                }
            }
            else{
                if(!policyPackDetailsMap.keySet().contains(parentPolicyPackId)) {
                    parentPolicyPackDetails= new PolicyPackDetails();
                    parentPolicyPackDetails.setId(parentPolicyPackId);
                    parentPolicyPackDetails.setName((String)map.get("parentPolicyPack"));
                    parentPolicyPackDetails.setDescription((String)map.get("parentPolicyPackDescription"));
                    parentPolicyPackDetails.setTopLevel((Integer)map.get("parentTopLevel"));
                    policyPackDetailsMap.put(parentPolicyPackId,parentPolicyPackDetails);
                }
                else {
                    parentPolicyPackDetails=policyPackDetailsMap.get(parentPolicyPackId);
                }
                if(!policyPackDetailsMap.keySet().contains(childPolicyPackId)){
                    childPolicyPackDetails= new PolicyPackDetails();
                    childPolicyPackDetails.setId((String)map.get("childPolicyPackId"));
                    childPolicyPackDetails.setName((String)map.get("childPolicyPack"));
                    childPolicyPackDetails.setTopLevel((Integer)map.get("childTopLevel"));
                    childPolicyPackDetails.setDescription((String)map.get("childPolicyPackDescription"));
                    policyPackDetailsMap.put(childPolicyPackId,childPolicyPackDetails);
                }
                else{
                    childPolicyPackDetails=policyPackDetailsMap.get(childPolicyPackId);
                }
                parentPolicyPackDetails.getChildPolicyPacks().add(childPolicyPackDetails);
            }
        }
        List topLevelPolicyPacks = policyPackDetailsMap.values().stream().filter(obj->Integer.valueOf(1).equals(obj.getTopLevel())).collect(Collectors.toList());
        LOGGER.debug("number of topLevelPolicyPacks: "+topLevelPolicyPacks.size());
        return topLevelPolicyPacks;
    }
}
