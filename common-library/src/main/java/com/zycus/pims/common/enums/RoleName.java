package com.zycus.pims.common.enums;

public enum RoleName {
    ADMIN,
    PROCUREMENT_MANAGER,
    PROCUREMENT_OFFICER,
    INVENTORY_MANAGER,
    APPROVER,
    EMPLOYEE;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
