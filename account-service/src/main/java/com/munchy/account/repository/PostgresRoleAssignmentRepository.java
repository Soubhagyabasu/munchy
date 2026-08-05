package com.munchy.account.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class PostgresRoleAssignmentRepository implements RoleAssignmentRepository {
    private final DatabaseClient databaseClient;

    public PostgresRoleAssignmentRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Void> assign(UUID userId, Short roleId) {
        return databaseClient.sql("""
                        INSERT INTO user_roles (user_id, role_id)
                        VALUES (:userId, :roleId)
                        ON CONFLICT (user_id, role_id) DO NOTHING
                        """)
                .bind("userId", userId)
                .bind("roleId", roleId)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Flux<String> findRoleNames(UUID userId) {
        return databaseClient.sql("""
                        SELECT r.name
                        FROM roles r
                        JOIN user_roles ur ON ur.role_id = r.id
                        WHERE ur.user_id = :userId
                        ORDER BY r.name
                        """)
                .bind("userId", userId)
                .map((row, metadata) -> row.get("name", String.class))
                .all();
    }
}
