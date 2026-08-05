package com.munchy.account.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHashServiceTests {
    @Test
    void hashesTokensDeterministicallyWithoutReturningTheRawValue() {
        TokenHashService service = new TokenHashService();

        String hash = service.sha256("secret-refresh-token");

        assertThat(hash).hasSize(64);
        assertThat(hash).isEqualTo(service.sha256("secret-refresh-token"));
        assertThat(hash).doesNotContain("secret-refresh-token");
    }
}
