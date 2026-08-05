package com.munchy.account.repository;

import com.munchy.account.entity.SessionLocationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SessionLocationRepository extends ReactiveCrudRepository<SessionLocationEntity, UUID> {
    Mono<SessionLocationEntity> findBySessionId(UUID sessionId);
}
