package com.munchy.account.service.impl;

import com.munchy.account.dto.location.CurrentLocationRequest;
import com.munchy.account.dto.location.CurrentLocationResponse;
import com.munchy.account.entity.AuthSessionEntity;
import com.munchy.account.entity.SessionLocationEntity;
import com.munchy.account.exception.InvalidSessionException;
import com.munchy.account.exception.ResourceNotFoundException;
import com.munchy.account.mapper.LocationMapper;
import com.munchy.account.repository.AuthSessionRepository;
import com.munchy.account.repository.SessionLocationRepository;
import com.munchy.account.service.LocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class LocationServiceImpl implements LocationService {
    private final SessionLocationRepository locations;
    private final AuthSessionRepository sessions;
    private final LocationMapper mapper;

    public LocationServiceImpl(
            SessionLocationRepository locations,
            AuthSessionRepository sessions,
            LocationMapper mapper) {
        this.locations = locations;
        this.sessions = sessions;
        this.mapper = mapper;
    }

    @Override
    public Mono<CurrentLocationResponse> get(UUID userId, UUID sessionId) {
        return requireOwnedActiveSession(userId, sessionId)
                .then(locations.findBySessionId(sessionId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Current location is not available")))
                .map(mapper::toResponse);
    }

    @Override
    public Mono<CurrentLocationResponse> update(
            UUID userId,
            UUID sessionId,
            CurrentLocationRequest request) {
        if (request.getCapturedAt().isAfter(Instant.now().plus(Duration.ofMinutes(5)))) {
            return Mono.error(new IllegalArgumentException("Location capture time cannot be in the future"));
        }

        return requireOwnedActiveSession(userId, sessionId)
                .then(locations.findBySessionId(sessionId)
                        .defaultIfEmpty(new SessionLocationEntity()))
                .flatMap(entity -> {
                    mapper.updateEntity(request, entity);
                    entity.setSessionId(sessionId);
                    entity.setSource("BROWSER_GPS");
                    entity.setUpdatedAt(Instant.now());
                    return locations.save(entity);
                })
                .map(mapper::toResponse);
    }

    private Mono<AuthSessionEntity> requireOwnedActiveSession(UUID userId, UUID sessionId) {
        Instant now = Instant.now();
        return sessions.findById(sessionId)
                .filter(session -> session.getUserId().equals(userId))
                .filter(session -> session.getRevokedAt() == null)
                .filter(session -> session.getExpiresAt().isAfter(now))
                .switchIfEmpty(Mono.error(new InvalidSessionException("Authentication session is not active")));
    }
}
