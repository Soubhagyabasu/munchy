package com.munchy.account.repository;

import com.munchy.account.entity.RoleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<RoleEntity, Short> {
    Mono<RoleEntity> findByName(String name);
}
