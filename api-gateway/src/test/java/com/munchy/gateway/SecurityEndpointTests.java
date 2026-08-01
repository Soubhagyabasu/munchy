package com.munchy.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static com.munchy.gateway.security.TokenCookieService.ACCESS_COOKIE;
import static com.munchy.gateway.security.TokenCookieService.REFRESH_COOKIE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SecurityEndpointTests {

    @Autowired
    private WebTestClient client;

    @Test
    void healthIsPublic() {
        client.get().uri("/api/v1/health").exchange().expectStatus().isOk();
    }

    @Test
    void ordersRequireAnAccessToken() {
        client.get().uri("/api/v1/orders").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void invalidBearerTokenIsRejected() {
        client.get().uri("/api/v1/orders")
                .headers(headers -> headers.setBearerAuth("not-a-jwt"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void refreshRequiresARefreshCookie() {
        client.post().uri("/api/v1/auth/refresh").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void logoutExpiresBothTokenCookies() {
        client.post().uri("/api/v1/auth/logout")
                .cookie(ACCESS_COOKIE, "token")
                .cookie(REFRESH_COOKIE, "refresh")
                .exchange()
                .expectStatus().isNoContent()
                .expectCookie().maxAge(ACCESS_COOKIE, Duration.ZERO)
                .expectCookie().maxAge(REFRESH_COOKIE, Duration.ZERO);
    }
}
