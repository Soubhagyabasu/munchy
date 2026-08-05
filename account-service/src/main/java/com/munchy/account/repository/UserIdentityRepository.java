package com.munchy.account.repository;

import com.munchy.account.entity.UserIdentityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserIdentityRepository extends ReactiveCrudRepository<UserIdentityEntity, UUID> {
    Mono<UserIdentityEntity> findByProviderAndProviderSubject(String provider, String providerSubject);
}
