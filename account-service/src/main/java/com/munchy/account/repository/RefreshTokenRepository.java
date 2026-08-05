package com.munchy.account.repository;

import com.munchy.account.entity.RefreshTokenEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshTokenEntity, UUID> {
    Mono<RefreshTokenEntity> findByJwtId(UUID jwtId);

    @Query("SELECT * FROM refresh_tokens WHERE jwt_id = :jwtId FOR UPDATE")
    Mono<RefreshTokenEntity> findForUpdateByJwtId(UUID jwtId);

    @Modifying
    @Query("""
            UPDATE refresh_tokens
            SET revoked_at = CURRENT_TIMESTAMP, revoke_reason = :reason
            WHERE session_id = :sessionId AND revoked_at IS NULL
            """)
    Mono<Integer> revokeActiveForSession(UUID sessionId, String reason);
}
