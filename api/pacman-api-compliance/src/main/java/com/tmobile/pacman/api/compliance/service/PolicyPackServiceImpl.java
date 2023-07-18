package com.tmobile.pacman.api.compliance.service;

import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.compliance.domain.PolicyPackDetails;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tmobile.pacman.api.commons.Constants.COMPLIANCEPERCENT;

@Component
public class PolicyPackServiceImpl implements PolicyPackService{

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyPackServiceImpl.class);
    private static final String INVALID_POLICY_PACKID="Policy pack id is invalid";
    private static final String ERROR_FETCHING_DATA="Error fetching data from ES.";

    private static final String BENCHMARK="benchmark";
    private static final String CATEGORY="category";
    private static Map<String,PolicyPackDetails> allPolicyPacksMap;

    @Value("${tagging.mandatoryTags}")
    private String mandatoryTags;
    @Autowired
    PacmanRdsRepository pacmanRdsRepository;

    public static Map<String, PolicyPackDetails> getAllPolicyPacksMap() {
        return allPolicyPacksMap;
    }

    public static void setAllPolicyPacksMap(Map<String, PolicyPackDetails> allPolicyPacksMap) {
        PolicyPackServiceImpl.allPolicyPacksMap = allPolicyPacksMap;
    }

    @Override
    public List<PolicyPackDetails> getAllPolicyPacks(String dataType) {
        LOGGER.info("inside getAllPolicyPacks() method of PolicyPackServiceImpl");
        String policyPackQueryString = "select p1.policyPackId as parentPolicyPackId, p1.policyPackName as parentPolicyPack, p1.policyPackDescription as parentPolicyPackDescription,\n" +
                " p1.topLevel as parentTopLevel,  p1.cloudProvider as parentCloudProvider, p1.policyPackCisVersionNumber as parentPpCisVersionNumber, p2.policyPackId as childPolicyPackId, p2.policyPackName as childPolicyPack, p2.policyPackDescription as childPolicyPackDescription," +
                " p2.topLevel as childTopLevel, p2.cloudProvider as childCloudProvider, p2.policyPackCisVersionNumber as childPpCisVersionNumber" +
                " from cf_PolicyPack p1, cf_PolicyPack p2, cf_PolicyPackInfo pp \n" +
                "where p1.policyPackId=pp.parentPolicyPackId and p2.policyPackId=pp.childPolicyPackId and p1.categoryPolicyPack=%d and p2.categoryPolicyPack=%d" +
                "" +
                " UNION " +
                "" +
                "select p1.policyPackId ,p1.policyPackName  ,p1.policyPackDescription ,p1.topLevel , p1.cloudProvider,p1.policyPackCisVersionNumber, null,null,null,null, null, null from cf_PolicyPack p1 where p1.categoryPolicyPack=%d";
        String accountQueryString="select distinct platform from cf_Accounts";
        if("category".equalsIgnoreCase(dataType))
            policyPackQueryString=String.format(policyPackQueryString,1,1,1);
        else if("benchmark".equalsIgnoreCase(dataType))
            policyPackQueryString=String.format(policyPackQueryString,0,0,0);

        List<Map<String, Object>> policyPackList = pacmanRdsRepository.getDataFromPacman(policyPackQueryString);
        List<Map<String, Object>> cloudProviderList =pacmanRdsRepository.getDataFromPacman(accountQueryString);
        ArrayList<String> cloudProviders=new ArrayList<>();
        for(Map<String, Object> map : cloudProviderList)
        {
            cloudProviders.add(map.get("platform").toString());
        }
        LOGGER.info("getAllPolicyPacks:::PolicyPackServiceImpl number of rows obtained from cf_PolicyPackInfo: "+policyPackList.size());
        Map<String,PolicyPackDetails> policyPackDetailsMap=new HashMap<>();
        for(Map<String, Object> map : policyPackList){
            String parentPolicyPackId = (String)map.get("parentPolicyPackId");
            String childPolicyPackId = (String)map.get("childPolicyPackId");
            PolicyPackDetails parentPolicyPackDetails= null;
            PolicyPackDetails childPolicyPackDetails=null;
            if(childPolicyPackId==null){
                if(!policyPackDetailsMap.keySet().contains(parentPolicyPackId)) {
                    parentPolicyPackDetails= getPolicyPackDetailsObject(map, parentPolicyPackId);
                    if(parentPolicyPackDetails.getCloudProvider()==null || cloudProviders.contains(parentPolicyPackDetails.getCloudProvider()))
                    {policyPackDetailsMap.put(parentPolicyPackId,parentPolicyPackDetails);}
                }
            }
            else{
                if(!policyPackDetailsMap.keySet().contains(parentPolicyPackId)) {
                    parentPolicyPackDetails= getPolicyPackDetailsObject(map, parentPolicyPackId);
                    if(parentPolicyPackDetails.getCloudProvider()==null || cloudProviders.contains(parentPolicyPackDetails.getCloudProvider()))
                    {policyPackDetailsMap.put(parentPolicyPackId,parentPolicyPackDetails);}
                }
                else {
                    parentPolicyPackDetails=policyPackDetailsMap.get(parentPolicyPackId);
                }
                if(!policyPackDetailsMap.keySet().contains(childPolicyPackId)){
                    childPolicyPackDetails= new PolicyPackDetails();
                    if(map.get("childCloudProvider")==null || cloudProviders.contains(map.get("childCloudProvider").toString())) {
                        childPolicyPackDetails.setId((String) map.get("childPolicyPackId"));
                        childPolicyPackDetails.setName((String) map.get("childPolicyPack"));
                        childPolicyPackDetails.setTopLevel((Integer) map.get("childTopLevel"));
                        childPolicyPackDetails.setDescription((String) map.get("childPolicyPackDescription"));
                        childPolicyPackDetails.setCloudProvider((String) map.get("childCloudProvider"));
                        childPolicyPackDetails.setPolicyPackVersionNumber((String) map.get("childPpCisVersionNumber"));
                        policyPackDetailsMap.put(childPolicyPackId, childPolicyPackDetails);
                    }
                }
                else{
                    childPolicyPackDetails=policyPackDetailsMap.get(childPolicyPackId);
                }
                if(childPolicyPackDetails.getId()!=null)
                {parentPolicyPackDetails.getChildPolicyPacks().add(childPolicyPackDetails);}
            }
        }
        setAllPolicyPacksMap(policyPackDetailsMap);
        List topLevelPolicyPacks = policyPackDetailsMap.values().stream().filter(obj->Integer.valueOf(1).equals(obj.getTopLevel())).collect(Collectors.toList());
        LOGGER.info("getAllPolicyPacks:::PolicyPackServiceImpl, number of topLevelPolicyPacks: "+topLevelPolicyPacks.size());
        return topLevelPolicyPacks;
    }

    private static PolicyPackDetails getPolicyPackDetailsObject(Map<String, Object> map, String parentPolicyPackId) {
        PolicyPackDetails parentPolicyPackDetails= new PolicyPackDetails();
        parentPolicyPackDetails.setId(parentPolicyPackId);
        parentPolicyPackDetails.setName((String) map.get("parentPolicyPack"));
        parentPolicyPackDetails.setDescription((String) map.get("parentPolicyPackDescription"));
        parentPolicyPackDetails.setTopLevel((Integer) map.get("parentTopLevel"));
        parentPolicyPackDetails.setCloudProvider((String) map.get("parentCloudProvider"));
        parentPolicyPackDetails.setPolicyPackVersionNumber((String) map.get("parentPpCisVersionNumber"));
        return parentPolicyPackDetails;
    }


    private Map<String,Map<String,String>> getPolicyIdsByPolicyPackId(List<String> leafPolicyPackList, String category) {
        String leafPolicyPackString = String.join(",",leafPolicyPackList.stream().map(str->"'"+str+"'").collect(Collectors.toList()));
        String policyPackQueryString = "select ppr.policyId,ppr.cisControlName,po.severity,ppr.policyCisVersionNumber from cf_PolicyPackRuleInfo ppr, cf_PolicyTable po where ppr.policyId=po.policyId and ppr.policyPackId in (%s)";
        policyPackQueryString=String.format(policyPackQueryString,leafPolicyPackString);
        if(category!=null){
            policyPackQueryString=policyPackQueryString.concat(" and lower(po.category)='"+category.toLowerCase()+"'");
        }
        List<Map<String, Object>> policyPackList = pacmanRdsRepository.getDataFromPacman(policyPackQueryString);
        Map<String,Map<String,String>> policyIdPolicyNameMap=new HashMap<>();
        policyPackList.stream().filter(mp-> mp.get("severity")!=null).forEach(mp->{
            if(!policyIdPolicyNameMap.containsKey((String)mp.get("policyId"))){
                Map<String,String> policyDetailsMap = new HashMap<>();
                policyDetailsMap.put("policyName",mp.get("cisControlName").toString());
                policyDetailsMap.put("severity",mp.get("severity").toString());
                policyDetailsMap.put("cisVersionNumber",mp.get("policyCisVersionNumber").toString());
                policyIdPolicyNameMap.put((String)mp.get("policyId"),policyDetailsMap);
            }
        });
        return policyIdPolicyNameMap;
    }
    /**
     *
     * @param sdf
     * @param datesWithDataPresent
     * @param maxDate
     * @param policyPackComplianceMap
     * @param latestCompDetailsList
     * @param policyIdPolicyDetailsMap
     */

    private static void populateLatestCompDetails(SimpleDateFormat sdf, Set<String> datesWithDataPresent, Date maxDate, Map<String, Object> policyPackComplianceMap, List<Map<String, Object>> latestCompDetailsList, Map<String, Map<String,String>> policyIdPolicyDetailsMap) {
        Map<String,Object> latestDetailsMap = policyPackComplianceMap.get(sdf.format(maxDate))==null?Collections.emptyMap():(Map<String,Object>) policyPackComplianceMap.get(sdf.format(maxDate));
        latestDetailsMap.keySet().stream().forEach(str -> {
            Map<String,Object> latestCompDetailsMap = new HashMap<>();
            latestCompDetailsMap.put("name",str);
            Map<String,Object> iMap = (Map<String,Object>)latestDetailsMap.get(str);
            latestCompDetailsMap.putAll(iMap);
            latestCompDetailsList.add(latestCompDetailsMap);
            if(policyIdPolicyDetailsMap!=null){
                Optional<String> severityStr = policyIdPolicyDetailsMap.values().stream().filter(detMap-> str.equalsIgnoreCase(detMap.get("policyName"))).map(val->val.get("severity")).findFirst();
               if(severityStr.isPresent()){
                   latestCompDetailsMap.put("severity",severityStr.get());
               }
            }
        });
        /* Remove all entries where polices are not run or data is not present. */
        policyPackComplianceMap.keySet().retainAll(datesWithDataPresent);
    }

    private void getLeafPolicyPack(PolicyPackDetails childPolicyPack, List<String> leafPolicyPackList) {
        if(childPolicyPack.getChildPolicyPacks().isEmpty()){
            leafPolicyPackList.add(childPolicyPack.getId());
        }
        for(PolicyPackDetails ppd : childPolicyPack.getChildPolicyPacks()){
            getLeafPolicyPack(ppd,leafPolicyPackList);
        }
    }

    private PolicyPackDetails getPolicyPackDetailsById(String policyPackId, List<PolicyPackDetails> policyPackList){
        for(PolicyPackDetails policyPackDetails : policyPackList){
            if(policyPackDetails.getId().equalsIgnoreCase(policyPackId))
                return policyPackDetails;
            else if(policyPackDetails.getChildPolicyPacks().size()>0){
                PolicyPackDetails ppd = getPolicyPackDetailsById(policyPackId,policyPackDetails.getChildPolicyPacks());
                if(ppd!=null)
                    return ppd;
            }
        }
        return null;
    }

    private void getAllPathsToLeafPolicyPacks(PolicyPackDetails policyPackDetails, List<String> leafPolicyPackPaths, String id) {
        if(policyPackDetails.getChildPolicyPacks().isEmpty()){
            leafPolicyPackPaths.add(id);
        }
        for(PolicyPackDetails ppd : policyPackDetails.getChildPolicyPacks()){
            getAllPathsToLeafPolicyPacks(ppd, leafPolicyPackPaths, id+ppd.getId()+".");
        }
    }
    /**
     * @param policyDetailsMap
     * @param compliant
     * @param total
     * @param acctOrTagsStr
     * @param keystr
     * @param tagSet
     * @param acctIdAcctNameMap
     */

    private void populateStatusCountDetailsInInnerMap(Map<String, Object> policyDetailsMap, Long compliant, Long total, String acctOrTagsStr, String keystr, Set<String> tagSet, Map<String, String> acctIdAcctNameMap){
        if(policyDetailsMap.containsKey(acctOrTagsStr)){
            Map<String, Object> innerMap = (Map<String, Object>)policyDetailsMap.get(acctOrTagsStr);
            if("By Tags".equalsIgnoreCase(acctOrTagsStr)){
                Optional tagNameOptional = tagSet.stream().filter(str -> keystr.startsWith("tags."+str+".")).findFirst();
                if(tagNameOptional.isPresent()){
                    String tagName = (String) tagNameOptional.get();
                    String tagValue = keystr.substring(6+tagName.length());
                    if(innerMap.containsKey(tagName)){
                        Map<String, Object> innerMap1 = (Map<String, Object>)innerMap.get(tagName);
                        if(innerMap1.containsKey(tagValue)){
                            Map<String, Object> innerMap2 = (Map<String, Object>)innerMap.get(tagValue);
                            innerMap2.put("compliant",compliant);
                            innerMap2.put("total",total);
                        }
                        else{
                            Map<String, Object> innerMap2=new HashMap<>();
                            innerMap1.put(tagValue,innerMap2);
                            innerMap2.put("compliant",compliant);
                            innerMap2.put("total",total);
                        }
                    }
                    else{
                        Map<String, Object> innerMap1=new HashMap<>();
                        Map<String, Object> innerMap2=new HashMap<>();
                        innerMap.put(tagName,innerMap1);
                        innerMap1.put(tagValue,innerMap2);
                        innerMap2.put("compliant",compliant);
                        innerMap2.put("total",total);
                    }
                }
            }
            else{
                if(innerMap.containsKey(keystr)){
                    Map<String, Object> innerMap1=(Map<String, Object>) innerMap.get(keystr);
                    if(!innerMap1.containsKey("accountName") && acctIdAcctNameMap.containsKey(keystr)){
                        innerMap1.put("accountName",acctIdAcctNameMap.get(keystr));
                    }
                    innerMap1.put("compliant",compliant);
                    innerMap1.put("total",total);
                }
                else{
                    Map<String, Object> innerMap1=new HashMap<>();
                    innerMap.put(keystr,innerMap1);
                    innerMap1.put("compliant",compliant);
                    innerMap1.put("total",total);
                    if(acctIdAcctNameMap.containsKey(keystr)){
                        innerMap1.put("accountName",acctIdAcctNameMap.get(keystr));
                    }
                }
            }
        }
        else{
            Map<String, Object> innerMap=new HashMap<>();
            Map<String, Object> innerMap1=new HashMap<>();
            Map<String, Object> innerMap2=new HashMap<>();
            policyDetailsMap.put(acctOrTagsStr,innerMap);
            if("By Tags".equalsIgnoreCase(acctOrTagsStr)){
                Optional tagNameOptional = tagSet.stream().filter(str -> keystr.startsWith("tags."+str+".")).findFirst();
                if(tagNameOptional.isPresent()){
                    String tagName = (String) tagNameOptional.get();
                    String tagValue = keystr.substring(6+tagName.length());

                    innerMap.put(tagName,innerMap1);
                    innerMap1.put(tagValue,innerMap2);
                    innerMap2.put("compliant",compliant);
                    innerMap2.put("total",total);
                }
            }
            else{
                innerMap.put(keystr,innerMap1);
                innerMap1.put("compliant",compliant);
                innerMap1.put("total",total);
                if(acctIdAcctNameMap.containsKey(keystr)){
                    innerMap1.put("accountName",acctIdAcctNameMap.get(keystr));
                }
            }
        }
    }

    /**
     *
     * @param ppackName
     * @param policyPackComplianceMap
     * @param mapObj
     */
    private void populateComplianceDetailsMap(String ppackName, Map<String,Object> policyPackComplianceMap, Map<String,Object> mapObj){
        Map<String, Object> innerMap1 = (Map<String, Object>)policyPackComplianceMap.get((String)mapObj.get("date"));
        Map<String, Object> innerMap2 = (Map<String, Object>)innerMap1.get(ppackName);
        if(innerMap2.containsKey("compliant")){
            innerMap2.put("compliant",(Double)innerMap2.get("compliant")+(Double)mapObj.get("compliant"));
        }else{
            innerMap2.put("compliant",(Double)mapObj.get("compliant"));
        }
        if(innerMap2.containsKey("total")){
            innerMap2.put("total",(Double)innerMap2.get("total")+(Double)mapObj.get("total"));
        }else{
            innerMap2.put("total",(Double)mapObj.get("total"));
        }
        if(((Double)innerMap2.get("total")).doubleValue()==0){
            innerMap2.put(COMPLIANCEPERCENT,0);
        }
        else{
            innerMap2.put(COMPLIANCEPERCENT,(Double)innerMap2.get("compliant")*100/(Double)innerMap2.get("total"));
        }

    }
    
    /**
    *
    * @param policyPackId
    * @return list of objects. Each object represents one recommendation id as key. Each object will contain
    * detailed information about CIS controls.
    * @throws Exception
    */
    public List<Map<String, Object>> getCISControls(String policyPackId) throws Exception {
        LOGGER.info(" policyPackId : {} ",policyPackId);
        String policyPackQueryString = "select cm.recommendationID as policyCisVersionNumber, cd.controlVersion, cd.cisSafegaurd, cd.title, cd.description, cd.ig1, cd.ig2, cd.ig3 "
                + " from cf_CISControlMapping cm join cf_CISControlDetails cd on cm.controlVersion = cd.controlVersion and cm.cisSafeguard = cd.cisSafegaurd where cm.policyPackID =  '%s' "
                + " order by cm.recommendationID, cd.cisSafegaurd, cd.controlVersion;";
        policyPackQueryString=String.format(policyPackQueryString,policyPackId);
        List<Map<String, Object>> policyPackList = pacmanRdsRepository.getDataFromPacman(policyPackQueryString);
        return policyPackList;
    }
    
    public Map<String, Object> getPolicyPackDetailsByID(String policyPackId) throws Exception {
        LOGGER.info(" policyPackId : {} ",policyPackId);
        String policyPackQueryString = "select policyPackId, policyPackName, policyPackDescription, cloudProvider from cf_PolicyPack where policyPackId = '%s'";
        policyPackQueryString=String.format(policyPackQueryString,policyPackId);
        List<Map<String, Object>> policyPackList = pacmanRdsRepository.getDataFromPacman(policyPackQueryString);
        return policyPackList != null ? policyPackList.get(0): null;
    }
    
    @SuppressWarnings("unused")
    private Map<String, Object> groupByControlVersion(List<Map<String, Object>> policyPackList){
        LOGGER.info("inside goupby control versions");
        Map<String,Object> recommendationMap = new HashMap<>();
         policyPackList.stream().forEach( controlDetails -> {
             Map<String,Object> controlMap = new HashMap<>();
             controlMap.put("policyCisVersionNumber", (String)controlDetails.get("policyCisVersionNumber"));
             controlMap.put("controlVersion", (String)controlDetails.get("controlVersion"));
             controlMap.put("cisSafegaurd", (String)controlDetails.get("cisSafegaurd"));
             controlMap.put("title", (String)controlDetails.get("title"));
             controlMap.put("description", (String)controlDetails.get("description"));
             controlMap.put("ig1", (Byte)controlDetails.get("ig1"));
             controlMap.put("ig2", (Byte)controlDetails.get("ig2"));
             controlMap.put("ig3", (Byte)controlDetails.get("ig3"));
            if(recommendationMap.containsKey(controlDetails.get("policyCisVersionNumber"))){
                List<Map<String,Object>> controlList = (List<Map<String,Object>>)recommendationMap.get(controlDetails.get("policyCisVersionNumber"));
                controlList.add(controlMap);
             } else {
                List<Map<String,Object>> controlList = new ArrayList<>();
                controlList.add(controlMap);
                recommendationMap.put((String)controlDetails.get("policyCisVersionNumber"), controlList);
             }
         });
         return recommendationMap;
    }
    
    /**
    *
    * @param policyID
    * @return list of policy standard objects. 
    * @throws Exception
    */
    public List<Map<String, Object>> getPolicyStandard(String policyID) throws Exception {
        LOGGER.info(" policyId : {} ",policyID);
        String policyStandardListQuery = "select pr.policyId as policyID, pr.standardName as standard, cm.controlVersion as controlVersion,"
        		+ " concat( cm.cisSafeguard, ' ', cd.title ) as controlName "
        		+ "from cf_PolicyPackRuleInfo  pr join cf_CISControlMapping cm on pr.policyPackRuleId = cm.policyPackRuleId "
        		+ "	 join cf_CISControlDetails cd on cm.controlVersion = cd.controlVersion and cm.cisSafeguard = cd.cisSafegaurd "
        		+ " where pr.policyId = '%s' ;";
        policyStandardListQuery=String.format(policyStandardListQuery,policyID);
         return pacmanRdsRepository.getDataFromPacman(policyStandardListQuery);
         
    }
}
