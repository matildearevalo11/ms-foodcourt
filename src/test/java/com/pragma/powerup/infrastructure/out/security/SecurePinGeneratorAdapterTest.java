package com.pragma.powerup.infrastructure.out.security;

import org.junit.jupiter.api.Test;
import java.security.SecureRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurePinGeneratorAdapterTest {
    @Test
    void generatePin_ShouldReturnSixDigitPin() {
        SecureRandom secureRandom = mock(SecureRandom.class);
        when(secureRandom.nextInt(1_000_000)).thenReturn(42);

        assertEquals("000042", new SecurePinGeneratorAdapter(secureRandom).generatePin());
    }
}
