package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.domain.FetchActivityLogsRequest;

import java.util.List;
import java.util.Map;

public interface ActivityLogService {
    List<Map<String,Object>> getActivityLogs(FetchActivityLogsRequest request, String dataSource, StringBuilder totalCount) throws Exception;

    List<Map<String,String>> getActivityLogFilterValues(String key) throws Exception;


}
