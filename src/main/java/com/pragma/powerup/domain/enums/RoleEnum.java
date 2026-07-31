package com.pragma.powerup.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ADMIN(1L, "ADMIN"),
    OWNER(2L, "OWNER"),
    EMPLOYEE(3L, "EMPLOYEE"),
    CUSTOMER(4L, "CUSTOMER");

    private final Long id;
    private final String name;

    public static RoleEnum fromString(String roleName) {
        for (RoleEnum role : values()) {
            if (role.name.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + roleName);
    }

    public static RoleEnum fromId(Long roleId) {
        for (RoleEnum role : values()) {
            if (role.id.equals(roleId)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role id: " + roleId);
    }
}
