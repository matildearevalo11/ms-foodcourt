package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.spi.IPinGeneratorPort;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurePinGeneratorAdapter implements IPinGeneratorPort {
    private static final int PIN_RANGE = 1_000_000;

    private final SecureRandom secureRandom;

    @Override
    public String generate() {
        return "%06d".formatted(secureRandom.nextInt(PIN_RANGE));
    }
}
