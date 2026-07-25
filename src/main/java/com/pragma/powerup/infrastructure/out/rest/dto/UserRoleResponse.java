package com.pragma.powerup.infrastructure.out.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRoleResponse(UserData data) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserData(Long id, String role) { }
}
