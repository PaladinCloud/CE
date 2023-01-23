package com.tmobile.pacman.api.commons.config;

import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RoleMappingLoader {

    public static final String ROLE_NAME = "role_name";
    public static final String PERMISSION_NAME = "permission_name";
    @Autowired
    private PacmanRdsRepository rdsRepository;

    private static final Logger log = LoggerFactory.getLogger(RoleMappingLoader.class);

    private Map<String,List<String>> roleList;


    public Map<String, List<String>> getRoleList() {
        return roleList;
    }

    public void setRoleList(Map<String,List<String>> roleList) {
        this.roleList = roleList;
    }

    private static final String QUERY="select r.role_name,p.permission_name from role r,  role_permissions rp,permission p\n" +
            "where r.role_id= rp.role_id\n" +
            "and rp.permission_id=p.permission_id";
    public RoleMappingLoader() {
        //default constructor
    }
    @PostConstruct
    public void runAfterObjectCreated() {
       log.info("Loading role-permission mapping.");
        Map<String,List<String>> rolePermissionMappings=new HashMap<>();
        log.info("Fetching role-mapping from DB. Query: {}",QUERY);
        List<Map<String, Object>> roleMappings = rdsRepository.getDataFromPacman(QUERY);
        roleMappings.stream().forEach(map->{
            String roleName=(String) map.get(ROLE_NAME);
            String permissionName=(String) map.get(PERMISSION_NAME);
            if(rolePermissionMappings.containsKey(roleName)){
                rolePermissionMappings.get(roleName).add(permissionName);
            }else{
                rolePermissionMappings.put(roleName, Stream.of(permissionName).collect(Collectors.toList()));
            }
        });
        rolePermissionMappings.entrySet().forEach(e->{
            log.info("Role Name: {}",e.getKey());
            log.info("Permissions: {}s",e.getValue());
        });
        this.setRoleList(rolePermissionMappings);
    }

}
