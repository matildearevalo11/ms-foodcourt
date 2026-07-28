package com.pragma.powerup.application.dto.response;

public record PageMetadataDto(int page, int size, long totalElements, int totalPages) {
}