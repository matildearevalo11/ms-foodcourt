package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDeliveryRequestDto {
    @NotBlank(message = "Security PIN is required")
    @Pattern(regexp = "^\\d{6}$", message = "Security PIN must contain 6 digits")
    private String securityPin;
}
