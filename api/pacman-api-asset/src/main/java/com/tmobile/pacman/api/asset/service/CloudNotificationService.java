package com.tmobile.pacman.api.asset.service;

import java.util.List;
import java.util.Map;

/**
 * This is the main interface for cloud notifications service which contains business logics and method calls to repository 
 */
public interface CloudNotificationService {

	public List<Map<String, Object>> getNotifications(String assetGroup, Map<String, String> filter, int size, int from);	
	public List<Map<String,Object>> getCloudNotificationsSummary(String assetGroup, boolean globalNotifier, String resourceId, String eventStatus);
	public Map<String,Object> getCloudNotificationDetail(String eventArn, String assetGroup);
	public Map<String,Object> getCloudNotificationInfo(String eventArn, boolean globalNotifier, String assetGroup);
	public Map<String,Object> getAutofixProjectionDetail(String assetGroup, Map<String, String> filter);
}
