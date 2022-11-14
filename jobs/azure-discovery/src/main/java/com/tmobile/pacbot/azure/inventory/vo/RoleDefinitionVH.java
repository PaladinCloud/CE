package com.tmobile.pacbot.azure.inventory.vo;

import java.util.List;
import java.util.Set;

public class RoleDefinitionVH extends AzureVH{

    private String roleName;

    private List<String>actions;

    private Set<String>assignableScopes;

    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public Set<String> getAssignableScopes() {
        return assignableScopes;
    }

    public void setAssignableScopes(Set<String> assignableScopes) {
        this.assignableScopes = assignableScopes;
    }

}
