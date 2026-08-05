package com.munchy.account.controller;

import com.munchy.account.dto.location.CurrentLocationRequest;
import com.munchy.account.dto.location.CurrentLocationResponse;
import com.munchy.account.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/current-location")
public class LocationController {
    private final LocationService locations;

    public LocationController(LocationService locations) {
        this.locations = locations;
    }

    @GetMapping
    public Mono<CurrentLocationResponse> get(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Auth-Session-Id") UUID sessionId) {
        return locations.get(userId, sessionId);
    }

    @PutMapping
    public Mono<CurrentLocationResponse> update(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Auth-Session-Id") UUID sessionId,
            @Valid @RequestBody CurrentLocationRequest request) {
        return locations.update(userId, sessionId, request);
    }
}
