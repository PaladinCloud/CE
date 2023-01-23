package com.tmobile.pacman.api.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CognitoUserService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public Map<String, Object> getUserInfo(CognitoIdentityProviderClient client, String userPoolId, String username){
        logger.info("Fetching user details from cognito. UserPoolId: {}, Username: {}",userPoolId,username);
        AdminGetUserResponse userDetails = client.adminGetUser(AdminGetUserRequest.builder().userPoolId(userPoolId).username(username).build());
        Map<String, Object> userInfo=new HashMap<>();
        userInfo.put("userId",userDetails.username());

        List<AttributeType> userAttributes = userDetails.userAttributes();
        userAttributes.forEach(attribute->{
            logger.info("Attribute name: {} value: {}",attribute.name(),attribute.value());
            if(attribute.name().equalsIgnoreCase("name")){
                userInfo.put("userName", attribute.value());
            } else if(attribute.name().equalsIgnoreCase("given_name")){
                userInfo.put("firstName", attribute.value());
            } else if(attribute.name().equalsIgnoreCase("family_name")){
                userInfo.put("lastName", attribute.value());
            } else if(attribute.name().equalsIgnoreCase("email")){
                userInfo.put("email", attribute.value());
            }else {
                userInfo.put(attribute.name(), attribute.value());
            }
        });
        return userInfo;
    }

    public GroupType createRole(CognitoIdentityProviderClient client, String userPoolId, String groupName){
        logger.info("Creating group: {} in cognito user pool: {}",groupName,userPoolId);
        CreateGroupResponse createGroupResponse = client.createGroup(CreateGroupRequest.builder()
                .userPoolId(userPoolId).groupName(groupName).build());
        if(createGroupResponse!=null){
            return createGroupResponse.group();
        }
        return null;
    }
    public List<String> getUserRoles(CognitoIdentityProviderClient client,String userPoolId, String userName){
        logger.info("Fetching groups of user: {}",userName);
        List<String> roles =new ArrayList<>();
                AdminListGroupsForUserResponse usersGroups = client.adminListGroupsForUser(AdminListGroupsForUserRequest
                .builder().userPoolId(userPoolId).username(userName).build());
        if(usersGroups!=null && !usersGroups.groups().isEmpty()) {
            roles = usersGroups.groups().stream().map(GroupType::groupName).collect(Collectors.toList());
        }
        return roles;
    }

    public String updateDefaultAssetGroup(CognitoIdentityProviderClient client,String userPoolId, String userName,
                                          String customAttributeName,String assetGroupValue) {
        logger.info("Updating users default asset group value");
        try {
            AdminUpdateUserAttributesResponse updateResponse = client.adminUpdateUserAttributes
                    (AdminUpdateUserAttributesRequest.builder().userPoolId(userPoolId).username(userName)
                            .userAttributes(AttributeType.builder().name(customAttributeName)
                                    .value(assetGroupValue).build()).build());
            logger.info("Response:{}",updateResponse);
        } catch (Exception e) {
            logger.error("Error in updating default asset group",e);
            return null;
        }
        return assetGroupValue;
    }

    public GroupType getGroup(CognitoIdentityProviderClient client, String userPoolId, String groupName){
        try {
            GetGroupResponse group = client.getGroup(GetGroupRequest.builder().userPoolId(userPoolId).
                    groupName(groupName).build());
            return group.group();
        } catch (AwsServiceException | SdkClientException e) {
            logger.error("Error in fetching group :{}",e.getMessage());
        }
        return null;
    }
    public void assignRole(CognitoIdentityProviderClient client, String userPoolId, String userName,String groupName){
        logger.info("Adding user:{} in group:{}",userName,groupName);
        try {
            AdminListGroupsForUserResponse usersGroups = client.adminListGroupsForUser(AdminListGroupsForUserRequest
                    .builder().userPoolId(userPoolId).username(userName).build());
            GroupType userGroup =null;
            if(usersGroups!=null && !usersGroups.groups().isEmpty()) {
                userGroup =usersGroups.groups().stream().filter(group -> group.groupName()
                        .equalsIgnoreCase(groupName)).findFirst().orElse(null);
            }
            if(userGroup!=null){
                logger.info("User:{} is already a member of the group:{}",userName,groupName);
            }else{
                //add user to group
                AdminAddUserToGroupResponse addUserResponse = client.adminAddUserToGroup(AdminAddUserToGroupRequest.builder().userPoolId(userPoolId)
                        .username(userName).groupName(groupName).build());
                logger.info("Add user resposne: {}",addUserResponse);
                logger.info("User:{} added to the group:{}",userName,groupName);
            }
        } catch (AwsServiceException |  SdkClientException e) {
            logger.error("Error in updating user group membership:{}",e.getMessage());
        }
    }

}
