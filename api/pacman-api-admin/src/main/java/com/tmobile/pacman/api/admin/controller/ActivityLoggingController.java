package com.tmobile.pacman.api.admin.controller;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.admin.domain.ActivityLogRequest;
import com.tmobile.pacman.api.admin.domain.FetchActivityLogsRequest;
import com.tmobile.pacman.api.admin.domain.Response;
import com.tmobile.pacman.api.admin.repository.PolicyRepository;
import com.tmobile.pacman.api.admin.repository.model.Policy;
import com.tmobile.pacman.api.admin.repository.service.ActivityLogService;
import com.tmobile.pacman.api.admin.util.AdminUtils;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Api(value = "/activitylog", consumes = "application/json", produces = "application/json")
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")
@RequestMapping("/activitylog")
public class ActivityLoggingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLoggingController.class);
    private static final String dataSource = "activitylog";
    private static final String ORDER_NOT_VALID="Order is not valid. It should be either 'asc' or 'desc'.";
    private static final String FIELD_NOT_VALID="Field is not valid.";
    private static final String FROM_DATE_AFTER_TO_DATE="From date cannot be after to date.";
    private static final String NO_DATA_FOUND="No activity log records are present.";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String FIELD_UPDATED_TIME = "Update Time";
    private static final String SORT_DESC = "desc";
    private static final String REQUEST_FIELD_UPDATED_TIME = "updateTime";
    private static final String SORT_FIELD_ACTION = "Action";
    private static final String UNABLE_TO_SORT_ERR_MSG = "Unable to sort due to unknown date format";

    @Value("${activitylogging.for.login.logout:no}")
    private String loginFlagForActivityLogging;

    @Autowired
    ElasticSearchRepository elasticSearchRepository;

    @Autowired
    ActivityLogService activityLogService;
    @Autowired
    private PolicyRepository policyRepository;

    @ApiOperation(httpMethod = "POST", value = "API to create activity log record", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/", method = RequestMethod.POST)
    public ResponseEntity<Object> logActivity(@RequestBody ActivityLogRequest request){
        try{
            LOGGER.info("inside ActivityLoggingController:::logActivity "+request.toString());
            List<String> missingAttributesList = new ArrayList<>();
            if(Strings.isNullOrEmpty(request.getObject())){
                missingAttributesList.add("object");
            }
            if(Strings.isNullOrEmpty(request.getObjectId())){
                missingAttributesList.add("objectId");
            }
            if(Strings.isNullOrEmpty(request.getUser())){
                missingAttributesList.add("user");
            }
            if(Strings.isNullOrEmpty(request.getAction())){
                missingAttributesList.add("action");
            }
            if(!missingAttributesList.isEmpty()){
                String missingAttrList = String.join(",",missingAttributesList);
                return ResponseUtils.buildFailureResponse(new Exception("Following attributes are not provided - "+missingAttrList),null, HttpStatus.BAD_REQUEST);
            }

            Map<String,Object> requestObject=request.getActivityLogDetails();
            if("Users".equalsIgnoreCase(request.getObject()) && ("login".equalsIgnoreCase(request.getAction()) || "logout".equalsIgnoreCase(request.getAction())) && !"yes".equalsIgnoreCase(loginFlagForActivityLogging)){
                return ResponseUtils.buildSucessResponse("Login/Logout is not be saved to activitylogging as related flag in configuration is turned off.");
            }
            else{
                boolean status= elasticSearchRepository.saveActivityLogToES(dataSource,requestObject);
                if(true)
                    return ResponseUtils.buildSucessResponse(new HashMap());
            }
        }
        catch(Exception exception){
            return ResponseUtils.buildFailureResponse(exception,null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseUtils.buildFailureResponse(new Exception("Activity log not saved to ES."),null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ApiOperation(httpMethod = "POST", value = "API to fetch activity log records", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    public ResponseEntity<Object>  getActivityLog(final HttpServletRequest servletRequest,
                                                  final HttpServletResponse servletResponse, @RequestBody FetchActivityLogsRequest request){
        try{
            LOGGER.info("inside ActivityLoggingController:::downloadActivityLogs");
            String order=request.getOrder();
            String field=request.getSortBy();
            LocalDateTime from=request.getFromDate();
            LocalDateTime to=request.getToDate();
            if(!Strings.isNullOrEmpty(order) && !("asc".equalsIgnoreCase(order) || "desc".equalsIgnoreCase(order))){
                return ResponseUtils.buildFailureResponse(new Exception(ORDER_NOT_VALID),null, HttpStatus.BAD_REQUEST);
            }
            if(!Strings.isNullOrEmpty(field) && !(Arrays.asList("updateTime","user","oldState","newState","object","objectId","action").contains(field))){
                return ResponseUtils.buildFailureResponse(new Exception(FIELD_NOT_VALID),null, HttpStatus.BAD_REQUEST);
            }
            if(from!=null && to!=null && from.compareTo(to)>0){
                return ResponseUtils.buildFailureResponse(new Exception(FROM_DATE_AFTER_TO_DATE),null, HttpStatus.BAD_REQUEST);
            }
            StringBuilder totalCount = new StringBuilder("0");
            List<Map<String,Object>>  activityDataList =  getActivityLogData(request,totalCount);
            Map<String, Object> response = new HashMap<>();
            response.put("records", activityDataList);
            response.put("totalCount",Long.valueOf(totalCount.toString()));
            return ResponseUtils.buildSucessResponse(response);
        }
        catch(Exception exception){
            LOGGER.error(exception.getMessage());
            return ResponseUtils.buildFailureResponse(exception,null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(httpMethod = "POST", value = "API to fetch activity log records for a policy", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/list/policy", method = RequestMethod.POST)
    public ResponseEntity<Object>  getActivityLogForPolicy(final HttpServletRequest servletRequest,
                                                  final HttpServletResponse servletResponse, @RequestBody FetchActivityLogsRequest request){
        try{
            LOGGER.info("inside ActivityLoggingController:::downloadActivityLogs");
            String order=request.getOrder();
            String field=request.getSortBy();
            LocalDateTime from=request.getFromDate();
            LocalDateTime to=request.getToDate();
            if(!Strings.isNullOrEmpty(order) && !("asc".equalsIgnoreCase(order) || "desc".equalsIgnoreCase(order))){
                return ResponseUtils.buildFailureResponse(new Exception(ORDER_NOT_VALID),null, HttpStatus.BAD_REQUEST);
            }
            if(!Strings.isNullOrEmpty(field) && !(Arrays.asList("updateTime","user","oldState","newState","object","objectId","action").contains(field))){
                return ResponseUtils.buildFailureResponse(new Exception(FIELD_NOT_VALID),null, HttpStatus.BAD_REQUEST);
            }
            if(from!=null && to!=null && from.compareTo(to)>0){
                return ResponseUtils.buildFailureResponse(new Exception(FROM_DATE_AFTER_TO_DATE),null, HttpStatus.BAD_REQUEST);
            }
            StringBuilder totalCount = new StringBuilder("0");
            List<Map<String,Object>>  activityDataList =  getActivityLogData(request,totalCount);
            Map<String, Object> response = new HashMap<>();
            response.put("records", activityDataList);
            response.put("totalCount",activityDataList.size());
            return ResponseUtils.buildSucessResponse(response);
        }
        catch(Exception exception){
            LOGGER.error(exception.getMessage());
            return ResponseUtils.buildFailureResponse(exception,null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Map<String,Object>> getActivityLogData(FetchActivityLogsRequest request, StringBuilder totalCount) throws Exception {
        if(Strings.isNullOrEmpty(request.getOrder())){
            request.setOrder("desc");
        }
        if(Strings.isNullOrEmpty(request.getSortBy())){
            request.setSortBy(REQUEST_FIELD_UPDATED_TIME);
        }
        if(request.getFromDate()==null){
            request.setFromDate(LocalDate.now(Clock.systemUTC()).atStartOfDay().minus(90, ChronoUnit.DAYS));
        }
        if(request.getToDate()==null){
            request.setToDate(LocalDateTime.now(Clock.systemUTC()));
        }

        List<Map<String, Object>> activityDataList=new ArrayList<>();
        List<Map<String, Object>> reqFieldsActivityDataList=new ArrayList<>();
        activityDataList=activityLogService.getActivityLogs(request,dataSource, totalCount);
        if(!request.getFilter().isEmpty() && request.getFilter().containsKey("objectId.keyword")){
            Policy isObjectPolicy = policyRepository.findByPolicyId(request.getFilter().get("objectId.keyword"));
            if(isObjectPolicy != null){
                reqFieldsActivityDataList.add(getActivityAuditLogObj(isObjectPolicy));
            }
        }
        activityDataList.stream().forEach(map -> {
            Map<String,Object> activityOrderedMap = new LinkedHashMap<>();
            activityOrderedMap.put("Object",map.get("object"));
            activityOrderedMap.put("Object Id",map.get("objectId"));
            activityOrderedMap.put("Action",map.get("action"));
            activityOrderedMap.put("Old State",map.get("oldState")==null?"NA":map.get("oldState"));
            activityOrderedMap.put("New State",map.get("newState")==null?"NA":map.get("newState"));
            activityOrderedMap.put("User",map.get("user"));
            activityOrderedMap.put("Update Time",map.get("updateTimeStr"));
            reqFieldsActivityDataList.add(activityOrderedMap);
        });
        if (StringUtils.isEmpty(request.getSortBy()) ||
                request.getSortBy().equalsIgnoreCase(REQUEST_FIELD_UPDATED_TIME)) {
            Comparator<Map<String, Object>> mapComparator = (m1, m2) -> {
                try {
                    Date date1 = AdminUtils.getFormatedDate(DATE_FORMAT, m1.get(FIELD_UPDATED_TIME).toString());
                    Date date2 = AdminUtils.getFormatedDate(DATE_FORMAT, m2.get(FIELD_UPDATED_TIME).toString());
                    if (StringUtils.isEmpty(request.getOrder()) || request.getOrder().equalsIgnoreCase(SORT_DESC)) {
                        return date2.compareTo(date1);
                    } else {
                        return date1.compareTo(date2);
                    }
                } catch (Exception e) {
                    LOGGER.error(UNABLE_TO_SORT_ERR_MSG, e);
                }
                return m1.get(SORT_FIELD_ACTION).toString().compareTo(m2.get(SORT_FIELD_ACTION).toString());
            };
            reqFieldsActivityDataList.sort(mapComparator);
        }
        return reqFieldsActivityDataList;
    }

    private Map<String,Object> getActivityAuditLogObj(Policy policy){
        String updatedTime = AdminUtils.getFormatedStringDate(DATE_FORMAT, policy.getCreatedDate());
        Map<String,Object> activityOrderedMap = new LinkedHashMap<>();
        activityOrderedMap.put("Object","Policy");
        activityOrderedMap.put("Object Id",policy.getPolicyId());
        activityOrderedMap.put("Action","creation");
        activityOrderedMap.put("Old State","Not existed");
        activityOrderedMap.put("New State",policy.getPolicyParams());
        activityOrderedMap.put("User",policy.getUserId());
        activityOrderedMap.put(FIELD_UPDATED_TIME, updatedTime);
        return activityOrderedMap;
    }


    @ApiOperation(httpMethod = "GET", value = "API to get filter keys of activity logging", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(path = "/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getFilterKeys(
            @ApiParam(value = "provide filter key", required = true) @RequestParam("key") String key) throws Exception {
        try{
            if(!Arrays.asList("action.keyword","object.keyword").contains(key)){
                return ResponseUtils.buildFailureResponse(new Exception("Invalid key"),null, HttpStatus.BAD_REQUEST);
            }

            return ResponseUtils.buildSucessResponse(activityLogService.getActivityLogFilterValues(key));
        }
        catch(Exception exception){
            LOGGER.error(exception.getMessage());
            return ResponseUtils.buildFailureResponse(exception,null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

