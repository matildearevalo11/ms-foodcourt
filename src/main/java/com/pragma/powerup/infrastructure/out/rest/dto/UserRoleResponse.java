package com.pragma.powerup.infrastructure.out.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRoleResponse(UserRoleData data) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserRoleData(Long id, UserRoleEnum role) { }
}
