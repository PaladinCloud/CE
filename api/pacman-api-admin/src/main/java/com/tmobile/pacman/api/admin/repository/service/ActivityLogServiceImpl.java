package com.tmobile.pacman.api.admin.repository.service;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.admin.domain.FetchActivityLogsRequest;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tmobile.pacman.api.commons.Constants.ES_PAGE_SIZE;

@Component
public class ActivityLogServiceImpl implements ActivityLogService {

    @Autowired
    ElasticSearchRepository elasticSearchRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLogServiceImpl.class);
    private static final String requestBodyForActivityLogs = "{\n"+
            "\"size\": <size>,\n"+
            "\"query\":{\n"+
            "  \"bool\":{\n"+
            "     \"filter\":[\n"+
            "           {\n"+
            "           \"range\":{\n"+
            "              \"updateTime\":{\n"+
            "                 \"gte\":\"%s\",\n"+
            "                 \"lte\":\"%s\",\n"+
            "                 \"format\":\"yyyy-MM-dd'T'HH:mm:ss\"\n"+
            "                       }\n"+
            "                      }\n"+
            "           }\n"+
            " filterConditions \n"+
            "         ]\n"+
            "      }}\n"+
            " sortConditions"+
            "     \n}";
    @Override
    public List<Map<String,Object>> getActivityLogs(FetchActivityLogsRequest request, String dataSource, StringBuilder totalCount) throws Exception {
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startDate = request.getFromDate().format(customFormatter);
        String endDate = request.getToDate().format(customFormatter);
        String requestBody = String.format(requestBodyForActivityLogs, startDate,endDate);
        Integer count = request.getFromNo()+request.getSize();
        String requestForCount = requestBody.replaceAll("\"size\": <size>,","");
        if(count > ES_PAGE_SIZE || count<=0){
            requestBody=requestBody.replaceAll("<size>",ES_PAGE_SIZE.toString());
        }
        else{
            requestBody=requestBody.replaceAll("<size>",count.toString());
        }

        requestForCount = requestForCount.replaceAll("sortConditions","");
        String sortConditions = ", \"sort\":[{\n"+
                "\"%s\" : {\n"+
                "\"order\" : \"%s\"\n"+
                "}}]\n";
        sortConditions = String.format(sortConditions, "updateTime".equalsIgnoreCase(request.getSortBy())?request.getSortBy():request.getSortBy()+".keyword",request.getOrder().toString());
        requestBody = requestBody.replaceAll("sortConditions",sortConditions);

        StringBuilder filters = new StringBuilder("");
        if(!request.getFilter().isEmpty()){
            Map<String, String> additionalFilters = request.getFilter();
            additionalFilters.entrySet().stream().forEach(obj -> {
                filters.append(",{\"term\":{\""+obj.getKey()+"\":\""+obj.getValue()+"\"}}");
            });
        }
        if(!Strings.isNullOrEmpty(request.getSearchText())){
            filters.append(", { \"bool\": {\"should\":[\n");
            filters.append("{\"wildcard\": {\n" +
                    "      \"updateTimeStr\":{\n" +
                    "        \"value\":\"*"+request.getSearchText()+"*\"\n" +
                    "      }\n" +
                    "}}, \n");
            filters.append("{\"wildcard\": {\n" +
                    "      \"object\":{\n" +
                    "        \"value\":\"*"+request.getSearchText()+"*\"\n" +
                    "      }\n" +
                    "}}, \n");
            filters.append("{\"wildcard\": {\n" +
                    "      \"objectId\":{\n" +
                    "        \"value\":\"*"+request.getSearchText()+"*\"\n" +
                    "      }\n" +
                    "}}, \n");
            filters.append("{\"wildcard\": {\n" +
                    "      \"oldState\":{\n" +
                    "        \"value\":\"*"+request.getSearchText()+"*\"\n" +
                    "      }\n" +
                    "}}, \n");
            filters.append("{\"wildcard\": {\n" +
                    "      \"newState\":{\n" +
                    "        \"value\":\"*"+request.getSearchText()+"*\"\n" +
                    "      }\n" +
                    "}}, \n");
            filters.append("{\"wildcard\": {\n" +
                    "      \"action\":{\n" +
                    "        \"value\":\"*"+request.getSearchText()+"*\"\n" +
                    "      }\n" +
                    "}}, \n");
            filters.append("{\"wildcard\": {\n" +
                    "      \"user\":{\n" +
                    "        \"value\":\"*"+request.getSearchText()+"*\"\n" +
                    "      }\n" +
                    "}}]}}");

        }
        if(filters.length()>0){
            requestBody=requestBody.replaceAll("filterConditions",filters.toString());
            requestForCount = requestForCount.replaceAll("filterConditions",filters.toString());
        }
        else{
            requestBody=requestBody.replaceAll("filterConditions","");
            requestForCount = requestForCount.replaceAll("filterConditions","");
        }

        LOGGER.info("inside ActivityLogServiceImpl:::getActivityLogs , request body is "+requestBody);
        LOGGER.info("inside ActivityLogServiceImpl:::getActivityLogs , request body for count is "+requestForCount);
        return elasticSearchRepository.fetchActivityLogs(dataSource, "", requestBody, requestForCount, request.getSize()==null ? 0 : request.getSize(), request.getFromNo()==null?0:request.getFromNo(), totalCount);
    }

    @Override
    public List<Map<String, String>> getActivityLogFilterValues(String key) throws Exception {

        List<Map<String,String>> returnList = new ArrayList<>();

        LocalDateTime from = LocalDate.now(Clock.systemUTC()).atStartOfDay().minus(90, ChronoUnit.DAYS);
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startDate = from.format(customFormatter);
        String endDate = LocalDateTime.now(Clock.systemUTC()).format(customFormatter);
        Map<String,Object> mustFilter = new HashMap<>();
        Map<String,Object> innerMustFilter = new HashMap<>();
        Map<String,Object> innerFilter = new HashMap<>();
        innerFilter.put("updateTime",innerMustFilter);
        innerMustFilter.put("gte",startDate);
        innerMustFilter.put("lte",endDate);
        innerMustFilter.put("format","yyyy-MM-dd'T'HH:mm:ss");
        mustFilter.put("range",innerFilter);


        Map<String, Long> totalDistributionForIndexAndType = elasticSearchRepository.getTotalDistributionForIndexAndType(
                "activitylog", null, mustFilter, null, null, key,
                ES_PAGE_SIZE, null);
        totalDistributionForIndexAndType.entrySet().stream().forEach(entry -> {
            Map<String,String> returnMap = new HashMap<>();
            returnMap.put("id",entry.getKey());
            returnMap.put("name",entry.getKey());
            returnList.add(returnMap);
        });

        return returnList;
    }
}

