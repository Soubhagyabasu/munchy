package com.munchy.account.repository;

import com.munchy.account.entity.UserAddressEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserAddressRepository extends ReactiveCrudRepository<UserAddressEntity, UUID> {
    Flux<UserAddressEntity> findByUserIdAndActiveTrueAndDeletedAtIsNullOrderByUpdatedAtDesc(UUID userId);

    Mono<UserAddressEntity> findByIdAndUserIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID userId);

    Mono<UserAddressEntity> findFirstByUserIdAndDefaultAddressTrueAndActiveTrueAndDeletedAtIsNull(UUID userId);

    Mono<UserAddressEntity> findFirstByUserIdAndActiveTrueAndDeletedAtIsNullOrderByUpdatedAtDesc(UUID userId);

    @Modifying
    @Query("UPDATE user_addresses SET is_default = FALSE WHERE user_id = :userId AND is_default = TRUE")
    Mono<Integer> clearDefaultForUser(UUID userId);
}
