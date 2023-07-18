package com.tmobile.pacman.api.admin.service;

import com.amazonaws.auth.BasicSessionCredentials;
import com.tmobile.pacman.api.admin.domain.CognitoUserDetails;
import com.tmobile.pacman.api.admin.domain.CognitoUserResponse;
import com.tmobile.pacman.api.admin.domain.CreateCognitoUserDetails;
import com.tmobile.pacman.api.admin.repository.model.CognitoUser;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

@Component
public class AmazonCognitoConnector {

    @Autowired
    private CredentialProvider credentialProvider;
    private static final Logger log = LoggerFactory.getLogger(AmazonCognitoConnector.class);
    private static final String USERPOOL_ID = System.getenv("USERPOOL_ID");
    private  static final String REGION = System.getenv("AWS_USERPOOL_REGION");
    private  static  final  String EMAIL="email";
    private  static  final  String GIVEN_NAME = "given_name";
    private  static  final  String FAMILY_NAME="family_name";

    private AdminGetUserResponse getUserByAccessToken(String accessToken) {
        CognitoIdentityProviderClient cognitoClient = getCognitoIdentityProviderClient();
        AdminGetUserRequest getRequest=AdminGetUserRequest.builder().userPoolId(USERPOOL_ID)
                .username(accessToken).build();
        return cognitoClient.adminGetUser(getRequest);
    }

    public CognitoUserResponse getCognitoUserDetails(String accessToken) {
        AdminGetUserResponse response = getUserByAccessToken(accessToken);
        CognitoUserResponse userFromCognito = new CognitoUserResponse();
        response.userAttributes().forEach(attr -> {
            if (attr.name().equalsIgnoreCase(GIVEN_NAME)) {
                userFromCognito.setFirstName(attr.value());
            } else if (attr.name().equalsIgnoreCase(FAMILY_NAME)) {
                userFromCognito.setLastName(attr.value());
            } else if (attr.name().equalsIgnoreCase(EMAIL)) {
                userFromCognito.setEmail(attr.value());
            }
        });
        return userFromCognito;
    }


    public List<CognitoUser> listAllUsers(Integer cursor, Integer limit, String filter) {

        List<CognitoUser> users = new ArrayList<>();

        ListUsersResponse result =null;
        List<UserType> userTypeList;
        boolean isUsersSkipped=false;
        ListUsersRequest.Builder requestBuilder = ListUsersRequest.builder().userPoolId(USERPOOL_ID).filter(filter);
        CognitoIdentityProviderClient identityProviderClient = getCognitoIdentityProviderClient();

        if(cursor>1){
            //Cursor not at the start, skipping the users before cursor
            result=identityProviderClient.listUsers(requestBuilder.limit(cursor-1).build());
            userTypeList = result.users();
            isUsersSkipped=true;
            log.info("Users to skip, as these are before cursor {}",userTypeList.size());
        }

        /*
         * as long as there is a pagination token in the list users result => resend
         * list users request with pagination token.
         */

        if(isUsersSkipped){
            String paginationToken = result.paginationToken();
            if (paginationToken != null) {
                log.info("inside pagination part {}", paginationToken);
                requestBuilder.paginationToken(paginationToken);
            }else{
                log.info("No users found");
                return users;
            }
        }
        if(limit!=null && limit>1){
            requestBuilder.limit(limit);
        }
        result = identityProviderClient.listUsers(requestBuilder.build());
        userTypeList = result.users();
        log.info("UserTypeList {}",userTypeList);
        users.addAll(userTypeList.stream().map(this::convertCognitoUser).collect(Collectors.toList()));
        return users;
    }

    protected CognitoUser convertCognitoUser(UserType awsCognitoUser) {
        CognitoUser.CognitoUserBuilder builder=CognitoUser.builder();
        builder.userName(awsCognitoUser.username());
        builder.roles(getRoles(awsCognitoUser.username()));
        builder.status(Boolean.TRUE.equals(awsCognitoUser.enabled())?"Active":"Inactive");

        for (AttributeType userAttribute : awsCognitoUser.attributes()) {
            String name = userAttribute.name();
            String value = userAttribute.value();
            if(name.equalsIgnoreCase(EMAIL)){
                builder.email(value);
                break;
            }
            log.info("Key {}", name);
            log.info("Value {}", value);
        }

        return builder.build();
    }

    private List<String> getRoles(String username){

        CognitoIdentityProviderClient identityProviderClient = getCognitoIdentityProviderClient();

        AdminListGroupsForUserResponse groupResult = identityProviderClient.adminListGroupsForUser(AdminListGroupsForUserRequest
                .builder().userPoolId(USERPOOL_ID).username(username)
                .build());

        List<GroupType> groups = groupResult.groups();

        List<String>roles=new ArrayList<>();

        for (GroupType group : groups) {
            String groupName = group.groupName();
            roles.add(groupName);
            log.info("groupName {}", groupName);
        }

        return  roles;
    }

    private CognitoIdentityProviderClient getCognitoIdentityProviderClient() {
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();

        return CognitoIdentityProviderClient.builder()
                .region(Region.of(REGION)).credentialsProvider(StaticCredentialsProvider
                        .create(AwsSessionCredentials
                                .create(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey(), credentials.getSessionToken()))).build();
    }

    public Map<String, Object> updateRoleMembership (String username, CognitoUserDetails details) {
        List<String> rolesToRemove = getRoles(username);
        List<String> roleNames = details.getRoles();
        //remove any existing roles which are not present in the latest role list
        rolesToRemove.removeAll(roleNames);
        if(!rolesToRemove.isEmpty()){
            removeRolesToUser(username,rolesToRemove);
        }
        return addRolesToUser(username,roleNames);
    }
    //add roles to user
    public Map<String, Object> addRolesToUser(String username, List<String>roleNames){

        CognitoIdentityProviderClient client=getCognitoIdentityProviderClient();

        for(String roleName:roleNames) {

            AdminAddUserToGroupRequest addRequest = AdminAddUserToGroupRequest.builder().userPoolId(USERPOOL_ID)
                    .username(username).groupName(roleName).build();

            client.adminAddUserToGroup(addRequest);
        }
        return createResponsemap(username, roleNames, "add");
    }

    private static Map<String, Object> createResponsemap(String username, List<String> roleNames, String action) {
        Map<String, Object> responseMap=new HashMap<>();
        responseMap.put("status","success");
        responseMap.put("roles", roleNames);
        responseMap.put("username", username);
        responseMap.put("action",action);
        return responseMap;
    }

    //remove roles to user
    public Map<String, Object> removeRolesToUser(String username, List<String>roleNames){
        CognitoIdentityProviderClient client=getCognitoIdentityProviderClient();
        for(String roleName:roleNames) {
            AdminRemoveUserFromGroupRequest removeRequest = AdminRemoveUserFromGroupRequest.builder()
                    .userPoolId(USERPOOL_ID).username(username)
                    .groupName(roleName).build();

            client.adminRemoveUserFromGroup(removeRequest);
        }
        return createResponsemap(username, roleNames, "remove");
    }


    //Edit user status
    public Object enableOrDisableUser(String username){
        CognitoIdentityProviderClient cognitoClient = getCognitoIdentityProviderClient();
        AdminGetUserRequest getRequest=AdminGetUserRequest.builder().userPoolId(USERPOOL_ID)
                .username(username).build();

        AdminGetUserResponse getResult = cognitoClient.adminGetUser(getRequest);
        boolean isUserEnabled=getResult.enabled();

        if(isUserEnabled){
            AdminDisableUserRequest disableUserRequest= AdminDisableUserRequest.builder().
                    userPoolId(USERPOOL_ID).username(username).build();
            cognitoClient.adminDisableUser(disableUserRequest);
            return "Status:Disabled";
        }
        else{
            AdminEnableUserRequest enableUserRequest=AdminEnableUserRequest.builder()
                    .userPoolId(USERPOOL_ID).username(username).build();
            cognitoClient.adminEnableUser(enableUserRequest);
            return "Status:Enabled";
        }
    }


    //create user
    public Map<String, Object> createUser(CreateCognitoUserDetails details){

        CognitoIdentityProviderClient cognitoClient = getCognitoIdentityProviderClient();

        String username=details.getUsername();

        List<AttributeType> attributeList=prepareUserAttributes(details.getUserAttributes());
        attributeList.add(AttributeType.builder().name(EMAIL).value(username).build());

        AdminCreateUserRequest createUserRequest= AdminCreateUserRequest.builder()
                .userPoolId(USERPOOL_ID)
                .username(username)
                .userAttributes(attributeList.toArray(new AttributeType[attributeList.size()])).build();
       cognitoClient.adminCreateUser(createUserRequest);

        AdminUpdateUserAttributesRequest updateUserAttributesRequest = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(USERPOOL_ID)
                .username(username)
                .userAttributes(AttributeType.builder().name("email_verified").value("true").build())
                .build();

        cognitoClient.adminUpdateUserAttributes(updateUserAttributesRequest);
        return createResponseMapForUser(username, "create");
    }

    private List<AttributeType> prepareUserAttributes(Map<String, Object> userAttributes) {
        List<AttributeType> attributesList=new ArrayList<>();
        if(userAttributes!=null && !userAttributes.isEmpty()) {
            userAttributes.forEach((key, value) -> {
                if (value != null) {
                    attributesList.add(AttributeType.builder().name(key).value((String) value).build());
                }
            });
        }
        return  attributesList;
    }

    //delete user
    public Map<String, Object> removeUser(String username){

        CognitoIdentityProviderClient cognitoClient = getCognitoIdentityProviderClient();

        AdminDeleteUserRequest deleteUserRequest=AdminDeleteUserRequest.builder().userPoolId(USERPOOL_ID)
                .username(username).build();

        cognitoClient.adminDeleteUser(deleteUserRequest);
        return createResponseMapForUser(username, "remove");
    }

    private static Map<String, Object> createResponseMapForUser(String email, String action) {
        Map<String, Object> responseMap=new HashMap<>();
        responseMap.put("status","success");
        responseMap.put(EMAIL, email);
        responseMap.put("action",action);
        return responseMap;
    }

    public  List<Map<String,Object>> listAllRoles(){
        List<Map<String,Object>>roleDetails = new ArrayList<>();
        CognitoIdentityProviderClient identityProviderClient = getCognitoIdentityProviderClient();
        try{
       ListGroupsResponse listGroupsResponse= identityProviderClient.listGroups(ListGroupsRequest.builder().userPoolId(USERPOOL_ID).build());
       List<GroupType> groups=listGroupsResponse.groups();

       for(GroupType group:groups) {
           Map<String, Object> responseMap = new HashMap<>();
           String groupName = group.groupName();
           responseMap.put("roleName", groupName);
           responseMap.put("isDefault", false);
           if (groupName.equalsIgnoreCase("ROLE_ADMIN") || groupName.equalsIgnoreCase("ROLE_USER")) {
               responseMap.put("isDefault", true);
           }
           String[] words = groupName.split("(?=[A-Z])");
           String displayName = String.join(" ", words);

           responseMap.put("roleDisplayName", displayName);

           roleDetails.add(responseMap);
           }
       } catch (CognitoIdentityProviderException e){
            log.error("Error occurred while listing groups %s", e.getMessage());
        }catch (Exception e){
            log.error(UNEXPECTED_ERROR_OCCURRED, e.getMessage());
        }
       return  roleDetails;
    }
}
