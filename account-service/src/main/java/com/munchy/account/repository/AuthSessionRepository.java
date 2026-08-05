package com.munchy.account.repository;

import com.munchy.account.entity.AuthSessionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface AuthSessionRepository extends ReactiveCrudRepository<AuthSessionEntity, UUID> {
}
