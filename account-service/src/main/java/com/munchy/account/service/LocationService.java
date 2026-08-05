package com.munchy.account.service;

import com.munchy.account.dto.location.CurrentLocationRequest;
import com.munchy.account.dto.location.CurrentLocationResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LocationService {
    Mono<CurrentLocationResponse> get(UUID userId, UUID sessionId);
    Mono<CurrentLocationResponse> update(UUID userId, UUID sessionId, CurrentLocationRequest request);
}
