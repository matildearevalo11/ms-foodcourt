package com.pragma.powerup.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ADMIN("ADMIN"),
    OWNER("OWNER"),
    EMPLOYEE("EMPLOYEE"),
    CUSTOMER("CUSTOMER");

    private final String name;

    public static RoleEnum fromString(String roleName) {
        for (RoleEnum role : values()) {
            if (role.name.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + roleName);
    }
}
