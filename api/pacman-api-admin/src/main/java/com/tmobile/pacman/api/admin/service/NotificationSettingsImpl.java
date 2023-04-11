package com.tmobile.pacman.api.admin.service;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.admin.domain.NotificationPrefsRequest;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NotificationSettingsImpl implements NotificationSettings{

    @Autowired
    PacmanRdsRepository pacmanRdsRepository;

    @Value("${notification.to.emailid:}")
    String toEmailId;

    private static final Logger log = LoggerFactory.getLogger(NotificationSettingsImpl.class);
    @Override
    public Map<String,Object> getNotificationSettings() {
        List<Map<String, Object>> notificationSettingsList =  pacmanRdsRepository.getDataFromPacman("select d.notificationTypeId,d.notificationType, d.notificationChannelId,d.channelName, a.notificationMappingId from (select p.notificationTypeId,p.notificationType,q.notificationChannelId,q.channelName from cf_NotificationTypes p, cf_NotificationChannels q) d LEFT JOIN cf_NotificationTypeChannelMapping a ON  \n" +
                "        d.notificationChannelId=a.notificationChannelId AND d.notificationTypeId=a.notificationTypeId");
        Map<String,Object> notificationFormattedMap = new HashMap<>();
        notificationSettingsList.stream().forEach(obj->{
            if(notificationFormattedMap.containsKey(obj.get("notificationType"))) {
                Map<String, Object> innerMap = (Map<String, Object>) notificationFormattedMap.get(obj.get("notificationType"));
                if (obj.get("notificationMappingId") == null) {
                    innerMap.put((String) obj.get("channelName"), 0);
                } else {
                    innerMap.put((String) obj.get("channelName"), 1);
                }
            }
            else{
                Map<String, Object> innerMap1 = new HashMap<>();
                notificationFormattedMap.put((String) obj.get("notificationType"), innerMap1);
                if (obj.get("notificationMappingId") == null) {
                    innerMap1.put((String) obj.get("channelName"), 0);
                } else {
                    innerMap1.put((String) obj.get("channelName"), 1);
                }
            }
        });
        return notificationFormattedMap;
    }

    @Override
    public void updateNotificationSettings(List<NotificationPrefsRequest> notificationPreferencesList) throws Exception {

        List<String>  notificationTypeList = notificationPreferencesList.stream().map(obj->obj.getNotificationType()).collect(Collectors.toList());
        List<String>  notificationChannelNameList = notificationPreferencesList.stream().map(obj->obj.getNotificationChannelName()).collect(Collectors.toList());

        List<Map<String,Object>> validNamesList = pacmanRdsRepository.getDataFromPacman("SELECT notificationType as name, notificationTypeId as pkey FROM cf_NotificationTypes union SELECT channelName as name, notificationChannelId as pkey FROM cf_NotificationChannels");

        List<String> validNotifTypesAndChannelsList = validNamesList.stream().map(obj->(String)obj.get("name")).collect(Collectors.toList());
        List<String> invalidNotificationTypeList = notificationTypeList.stream().filter(str -> !validNotifTypesAndChannelsList.contains(str)).collect(Collectors.toList());
        List<String> invalidNotificationChannelNamesList = notificationChannelNameList.stream().filter(str -> !validNotifTypesAndChannelsList.contains(str)).collect(Collectors.toList());
        Map<String,String> nameAndIdMap = validNamesList.stream().collect(Collectors.toMap(obj->(String)((Map)obj).get("name"), obj->(String)((Map)obj).get("pkey")));
        StringBuilder errorMessage = new StringBuilder("");
        if(invalidNotificationTypeList.size()>0 ){
            errorMessage.append("Invalid notification types - "+String.join(",",invalidNotificationTypeList));
        }
        if(invalidNotificationChannelNamesList.size()>0){
            errorMessage.append(" Invalid notification channels - "+String.join(",",invalidNotificationChannelNamesList));
        }
        if(errorMessage.length()>0){
            throw new Exception(errorMessage.toString());
        }


        boolean insertMappingsPresent=false;
        boolean removeMappingPresent=false;
        StringBuilder insertNotifMappingsQuery = new StringBuilder("INSERT IGNORE INTO cf_NotificationTypeChannelMapping VALUES ");
        StringBuilder deleteNotifMappingsQuery = new StringBuilder("DELETE FROM cf_NotificationTypeChannelMapping WHERE (");
        for(NotificationPrefsRequest notificationPrefsRequest: notificationPreferencesList) {
            if ("add".equalsIgnoreCase(notificationPrefsRequest.getAddOrRemove())) {
                insertMappingsPresent = true;
                insertNotifMappingsQuery.append(" ('"+UUID.randomUUID().toString()+"','"+nameAndIdMap.get(notificationPrefsRequest.getNotificationType())+"','"+nameAndIdMap.get(notificationPrefsRequest.getNotificationChannelName())+"','"+notificationPrefsRequest.getUpdatedBy()+"',current_timestamp()),");

            } else {
                removeMappingPresent = true;
                deleteNotifMappingsQuery.append("(notificationTypeId='"+nameAndIdMap.get(notificationPrefsRequest.getNotificationType())+"' AND notificationChannelId='"+nameAndIdMap.get(notificationPrefsRequest.getNotificationChannelName())+"') OR");
            }
        }
        if(insertMappingsPresent) {
            insertNotifMappingsQuery.setLength(insertNotifMappingsQuery.length() - 1);
            pacmanRdsRepository.update(insertNotifMappingsQuery.toString());
        }
        if(removeMappingPresent) {
            deleteNotifMappingsQuery.setLength(deleteNotifMappingsQuery.length()-2);
            deleteNotifMappingsQuery.append(")");
            pacmanRdsRepository.update(deleteNotifMappingsQuery.toString());
        }
    }
    @Override
    public Map<String,Object> getNotificationSettingsAndConfigs() {
        Map<String,Object> notfSettingsAndConfigMap =new HashMap<>();
        Map<String,Object> notificationTypesMap = getNotificationSettings();
        for(String notificationType : notificationTypesMap.keySet()){
            Map<String,Object> channelsMap = (Map<String,Object>) notificationTypesMap.get(notificationType);
            for(String channel : channelsMap.keySet()){
                Map<String,Object> channelConfigMap = new HashMap<>();
                channelConfigMap.put("isOn",Integer.parseInt(channelsMap.get(channel).toString()));
                if("email".equalsIgnoreCase(channel)){
                    if(!Strings.isNullOrEmpty(toEmailId)){
                        String[] emailIdArray = toEmailId.split(",");
                        channelConfigMap.put("toAddress",Arrays.asList(emailIdArray));
                    }
                    else{
                        channelConfigMap.put("toAddress",Collections.emptyList());
                    }
                }
                else{
                    channelConfigMap.put("toAddress",Collections.emptyList());
                }
                channelsMap.put(channel,channelConfigMap);
            }
        }
        notfSettingsAndConfigMap.put("settings",notificationTypesMap);
        return notfSettingsAndConfigMap;
    }

}
