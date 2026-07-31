package com.pragma.powerup.infrastructure.out.rest.dto;

public record OrderReadyNotificationRequest(String phoneNumber, String securityPin) {
}
