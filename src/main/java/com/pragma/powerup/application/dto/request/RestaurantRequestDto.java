package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantRequestDto {
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^\\d*\\D.*$", message = "Restaurant name cannot contain only numbers")
    private String name;

    @NotBlank(message = "NIT is required")
    @Pattern(regexp = "\\d+", message = "NIT must contain only numbers")
    private String nit;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone is required")
    @Size(max = 13, message = "Restaurant phone must be at most 13 characters")
    @Pattern(regexp = "\\+?\\d+", message = "Restaurant phone has an invalid format")
    private String phone;

    @NotBlank(message = "Logo URL is required")
    private String urlLogo;

    @NotNull(message = "Owner id is required")
    @Positive(message = "Owner id must be positive")
    private Long ownerId;
}
