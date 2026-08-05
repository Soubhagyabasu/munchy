package com.munchy.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/v1/account/health")
    public Mono<Map<String, String>> health() {
        return Mono.just(Map.of("status", "UP", "service", "account-service"));
    }
}
