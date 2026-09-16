package com.pragma.powerup.infrastructure.out.rest.dto;

public record UserContactResponse(UserContactData data) {
    public record UserContactData(Long id, String cellphone) { }
}
