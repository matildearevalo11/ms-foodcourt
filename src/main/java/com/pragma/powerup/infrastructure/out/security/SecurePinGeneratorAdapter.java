package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.spi.IPinGeneratorPort;
import java.security.SecureRandom;

public class SecurePinGeneratorAdapter implements IPinGeneratorPort {
    private static final int PIN_NUMBER_RANGE = 1_000_000;
    private final SecureRandom secureRandom;

    public SecurePinGeneratorAdapter(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Override
    public String generatePin() {
        return "%06d".formatted(secureRandom.nextInt(PIN_NUMBER_RANGE));
    }
}
