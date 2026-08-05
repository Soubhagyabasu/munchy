package com.munchy.account.security;

import java.time.Instant;
import java.util.UUID;

public record IssuedTokenPair(
        String accessToken,
        String refreshToken,
        UUID refreshJwtId,
        Instant refreshExpiresAt
) {
}
