package com.pragma.powerup.infrastructure.out.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserContactResponse(UserContactData data) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserContactData(Long id, String cellphone) { }
}
