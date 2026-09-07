package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RestaurantRequestDto(

        @NotBlank(message = "Name is required")
        @Pattern(regexp = "^(?!\\s*\\d+\\s*$).+", message = "Restaurant name cannot contain only numbers")
        String name,

        @NotBlank(message = "NIT is required")
        @Pattern(regexp = "\\d+", message = "NIT must contain only numbers")
        String nit,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Phone is required")
        @Size(max = 13, message = "Restaurant phone must be at most 13 characters")
        @Pattern(regexp = "\\+?\\d+", message = "Restaurant phone has an invalid format")
        String phone,

        @NotBlank(message = "Logo URL is required")
        String urlLogo,

        @NotNull(message = "Owner id is required")
        @Positive(message = "Owner id must be positive")
        Long ownerId
) { }
