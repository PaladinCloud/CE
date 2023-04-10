package com.tmobile.pacman.api.admin.repository.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tmobile.pacman.api.admin.domain.CreateAssetGroup;
import com.tmobile.pacman.api.admin.domain.DataSourceObj;
import com.tmobile.pacman.api.admin.repository.DatasourceRepository;
import com.tmobile.pacman.api.admin.repository.TargetTypesRepository;
import com.tmobile.pacman.api.admin.service.CommonService;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

@Service
public class CreateAssetGroupServiceImpl implements CreateAssetGroupService {
    private static final Logger log = LoggerFactory.getLogger(CreateAssetGroupServiceImpl.class);
    @Autowired
    private TargetTypesRepository targetTypesRepository;
    @Autowired
    private CommonService commonService;


    @Autowired
    private DatasourceRepository datasourceRepository;

    private List<Object> createFilterForDataSource(List<DataSourceObj> dataSourceObjList, String aliasName){
        List<Object> action = Lists.newArrayList();
        for(DataSourceObj obj : dataSourceObjList){
            String ds = obj.getDsName();
            if(ifIndexValid(obj.getIndexName())){
                Map<String, Object> addObj = Maps.newHashMap();
                Map<String, Object> add = Maps.newHashMap();
                List<Map<String, String>> should = obj.getShould();
                List<Object> shouldArray = Lists.newArrayList();
                for(Map<String,String> andMap : should){
                    shouldArray.add(buildMustArray(andMap));
                }
                Map<String, Object> boolObj = Maps.newHashMap();
                Map<String, Object> filterDetails = Maps.newHashMap();
                boolObj.put("should", shouldArray);
                filterDetails.put("bool", boolObj);
                if(filterDetails != null){
                    addObj.put("filter", filterDetails);
                }
                addObj.put("index",obj.getIndexName());
                addObj.put("alias",aliasName);
                add.put("add", addObj);
                action.add(add);
            }
        }
        return action;
    }

    private boolean ifIndexValid(String index){
        log.info("Checking if the provided index exits or not ::", index);
        try{
            Response res = commonService.invokeAPI("GET", index, null);
            if(res != null){
                log.info("Provided index exits ::", index);
                return true;
            }
        }catch(Exception e){
            log.error("Provided index does not exits ::", index);
            return false;
        }
        log.info("Provided index does not exits ",index);
        return false;
    }

    public CreateAssetGroup createAliasForAssetGroup(final CreateAssetGroup assetGroupDetailsJson) {
        log.info("Creating alias query for request ::", assetGroupDetailsJson);
        Map<String, Object> alias = Maps.newHashMap();
        try{
            String aliasName = assetGroupDetailsJson.getGroupName().toLowerCase().trim().replaceAll(" ","-");
            List<Object> action = Lists.newArrayList();
            List<String> datasourceListFromDb = datasourceRepository.getAllDataSourceList();
            log.info("Datasource list from db is ::", datasourceListFromDb);
            List<DataSourceObj> dataSourceObjList = new ArrayList<>();
            List<HashMap<String, Object>> configurationList = assetGroupDetailsJson.getConfiguration() != null ? assetGroupDetailsJson.getConfiguration() : new ArrayList<HashMap<String, Object>>();
            if(CollectionUtils.isEmpty(configurationList)){
                for(String ds : datasourceListFromDb){
                    DataSourceObj obj = new DataSourceObj();
                    obj.setIndexName(ds+"_*");
                    obj.setDsName(ds);
                    dataSourceObjList.add(obj);
                }
            }
            if(!CollectionUtils.isEmpty(configurationList)){
                Set<String> dataSourceLs = new HashSet<>();
                boolean isCloudTypeSpecified = true;
                for(Map<String, Object> configuration : configurationList){
                    String ds = configuration.containsKey("CloudType") ? ((String) configuration.get("CloudType")).toLowerCase() : "";
                    String targetType = configuration.containsKey("TargetType") ? ((String) configuration.get("TargetType")).toLowerCase() : "";
                    String dsFromTargetType = "";
                    dsFromTargetType = StringUtils.isEmpty(targetType) ? "" : targetTypesRepository.findDataSourceByTargetType(targetType).toLowerCase();
                    dataSourceLs.add(StringUtils.isEmpty(dsFromTargetType) ? ds : dsFromTargetType+"_"+targetType);
                    if(StringUtils.isEmpty(ds) && StringUtils.isEmpty(dsFromTargetType)){
                        isCloudTypeSpecified = false;
                        break;
                    }
                }
                if(!isCloudTypeSpecified){
                    dataSourceLs = new HashSet<>();
                    for(String s : datasourceListFromDb){
                        dataSourceLs.add(s.toLowerCase());
                    }
                }
                for(String dataSrc : dataSourceLs){
                    DataSourceObj obj = new DataSourceObj();
                    obj.setDsName(dataSrc);
                    for(Map<String, Object> configuration : configurationList){
                        String curDsTemp = configuration.containsKey("CloudType") ? (String) configuration.get("CloudType") : "";
                        String targetType = configuration.containsKey("TargetType") ? (String) configuration.get("TargetType") : "";
                        String dsFromTargetType = "";
                        dsFromTargetType = StringUtils.isEmpty(targetType) ? "" : targetTypesRepository.findDataSourceByTargetType(targetType).toLowerCase();
                        String curDs = StringUtils.isEmpty(dsFromTargetType) ? curDsTemp : dsFromTargetType+"_"+targetType;
                        if((!StringUtils.isEmpty(curDs) && curDs.equalsIgnoreCase(dataSrc)) || StringUtils.isEmpty(curDs)){
                            obj.setIndexName(!StringUtils.isEmpty(curDs) ?curDs+"_*" : dataSrc+"_*");
                            if(!StringUtils.isEmpty(targetType)) {
                                obj.setDsName(dsFromTargetType);
                               obj.setIndexName(curDs);
                            }
                            LinkedHashMap<String, String> map = new LinkedHashMap<>();
                            configuration.forEach((key, value) -> {
                                if(!key.equalsIgnoreCase("CloudType") && !key.equalsIgnoreCase("TargetType")){
                                    map.put(key, value.toString());
                                }
                            });
                            obj.getShould().add(map);
                        }
                    }
                    dataSourceObjList.add(obj);
                }
            }
            List<Object> ls = createFilterForDataSource(dataSourceObjList, aliasName);
            if(!CollectionUtils.isEmpty(ls)){
                action.addAll(ls);
            }
            alias.put("actions", action);
            log.info("Alias query created ::"+alias);
            assetGroupDetailsJson.setAlias(alias);
        }catch(Exception exception){
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return assetGroupDetailsJson;
        }
        return assetGroupDetailsJson;
    }
    private Map<String, Object> buildMustArray(Map<String, String> attributes) {
        List<Object> mustArray = Lists.newArrayList();
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            Map<String, Object> attributeObj = Maps.newHashMap();
            attributeObj.put(attribute.getKey() + ".keyword", attribute.getValue());
            Map<String, Object> match = Maps.newHashMap();
            match.put("match", attributeObj);
            mustArray.add(match);
        }
        Map<String, Object> mustObj = Maps.newHashMap();
        mustObj.put("must", mustArray);
        Map<String, Object> innerboolObj = Maps.newHashMap();
        innerboolObj.put("bool", mustObj);
        return innerboolObj;
    }
}
