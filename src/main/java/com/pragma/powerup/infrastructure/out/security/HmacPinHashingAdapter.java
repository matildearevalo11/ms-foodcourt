package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.spi.IPinHashingPort;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacPinHashingAdapter implements IPinHashingPort {
    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKey;

    public HmacPinHashingAdapter(String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("Order PIN secret must contain at least 32 characters");
        }
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    @Override
    public String hash(String pin) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);
            return HexFormat.of().formatHex(mac.doFinal(pin.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Security PIN could not be protected", exception);
        }
    }
}
