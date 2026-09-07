package com.pragma.powerup.application.dto.response;

public record RestaurantResponseDto(Long id, String name, String nit, String address, String phone,
                                    String urlLogo, Long ownerId) { }
