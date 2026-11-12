package com.zycus.pims.auth.dto;

import com.zycus.pims.common.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class AssignRolesRequest {

    @NotEmpty(message = "At least one role must be specified")
    private Set<RoleName> roles;

    public AssignRolesRequest() {
    }

    public AssignRolesRequest(Set<RoleName> roles) {
        this.roles = roles;
    }

    public Set<RoleName> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleName> roles) {
        this.roles = roles;
    }
}
