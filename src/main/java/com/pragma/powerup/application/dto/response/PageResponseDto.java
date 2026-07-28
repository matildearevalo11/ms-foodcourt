package com.pragma.powerup.application.dto.response;

import java.util.List;

public record PageResponseDto<T>(List<T> data, PageMetadataDto meta) {
}