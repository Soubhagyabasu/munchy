package com.munchy.account.service.impl;

import com.munchy.account.dto.auth.GoogleLoginRequest;
import com.munchy.account.dto.auth.TokenPairResponse;
import com.munchy.account.dto.user.AccountUserResponse;
import com.munchy.account.entity.AuthSessionEntity;
import com.munchy.account.entity.RefreshTokenEntity;
import com.munchy.account.entity.UserEntity;
import com.munchy.account.entity.UserIdentityEntity;
import com.munchy.account.exception.InvalidSessionException;
import com.munchy.account.exception.ResourceNotFoundException;
import com.munchy.account.mapper.UserMapper;
import com.munchy.account.repository.AuthSessionRepository;
import com.munchy.account.repository.RefreshTokenRepository;
import com.munchy.account.repository.RoleAssignmentRepository;
import com.munchy.account.repository.RoleRepository;
import com.munchy.account.repository.UserIdentityRepository;
import com.munchy.account.repository.UserRepository;
import com.munchy.account.security.IssuedTokenPair;
import com.munchy.account.security.JwtTokenService;
import com.munchy.account.security.TokenHashService;
import com.munchy.account.service.AuthenticationService;
import com.munchy.account.service.UserService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final String GOOGLE = "GOOGLE";
    private static final String CUSTOMER = "CUSTOMER";

    private final UserRepository users;
    private final UserIdentityRepository identities;
    private final RoleRepository roles;
    private final RoleAssignmentRepository roleAssignments;
    private final AuthSessionRepository sessions;
    private final RefreshTokenRepository refreshTokens;
    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtTokenService jwtTokens;
    private final TokenHashService tokenHashes;
    private final TransactionalOperator transactions;

    public AuthenticationServiceImpl(
            UserRepository users,
            UserIdentityRepository identities,
            RoleRepository roles,
            RoleAssignmentRepository roleAssignments,
            AuthSessionRepository sessions,
            RefreshTokenRepository refreshTokens,
            UserService userService,
            UserMapper userMapper,
            JwtTokenService jwtTokens,
            TokenHashService tokenHashes,
            TransactionalOperator transactions) {
        this.users = users;
        this.identities = identities;
        this.roles = roles;
        this.roleAssignments = roleAssignments;
        this.sessions = sessions;
        this.refreshTokens = refreshTokens;
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtTokens = jwtTokens;
        this.tokenHashes = tokenHashes;
        this.transactions = transactions;
    }

    @Override
    public Mono<TokenPairResponse> loginWithGoogle(GoogleLoginRequest request) {
        Mono<TokenPairResponse> operation = findOrCreateGoogleAccount(request)
                .flatMap(account -> createSession(account, request));
        return transactions.transactional(operation);
    }

    @Override
    public Mono<TokenPairResponse> refresh(String rawRefreshToken) {
        return jwtTokens.validateRefresh(rawRefreshToken)
                .onErrorMap(error -> new InvalidSessionException("Refresh token is invalid or expired"))
                .flatMap(jwt -> transactions.transactional(rotate(jwt, rawRefreshToken)))
                .onErrorResume(RefreshTokenReuseException.class, reuse ->
                        transactions.transactional(revokeSession(reuse.sessionId(), "REFRESH_TOKEN_REUSE"))
                                .then(Mono.error(new InvalidSessionException(
                                        "Refresh-token reuse was detected; the session was revoked"))));
    }

    @Override
    public Mono<Void> logout(String rawRefreshToken) {
        return jwtTokens.validateRefresh(rawRefreshToken)
                .flatMap(jwt -> revokeSession(requiredUuidClaim(jwt, "sid"), "LOGOUT"))
                .as(transactions::transactional)
                .onErrorResume(error -> Mono.empty());
    }

    private Mono<ResolvedAccount> findOrCreateGoogleAccount(GoogleLoginRequest request) {
        return identities.findByProviderAndProviderSubject(GOOGLE, request.getGoogleSubject())
                .flatMap(identity -> updateExistingAccount(identity, request))
                .switchIfEmpty(Mono.defer(() -> createGoogleAccount(request)));
    }

    private Mono<ResolvedAccount> updateExistingAccount(
            UserIdentityEntity identity,
            GoogleLoginRequest request) {
        Instant now = Instant.now();
        return users.findById(identity.getUserId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Google identity points to a missing Munchy user")))
                .flatMap(user -> {
                    user.setEmail(request.getEmail());
                    user.setName(request.getName());
                    user.setPictureUrl(request.getPictureUrl());
                    user.setLastLoginAt(now);
                    user.setUpdatedAt(now);
                    identity.setProviderEmail(request.getEmail());
                    identity.setEmailVerified(request.isEmailVerified());
                    identity.setLastLoginAt(now);
                    return users.save(user)
                            .then(identities.save(identity))
                            .then(resolveAccount(user, identity.getId()));
                });
    }

    private Mono<ResolvedAccount> createGoogleAccount(GoogleLoginRequest request) {
        Instant now = Instant.now();
        UserEntity user = userMapper.fromGoogle(request);
        user.setStatus("ACTIVE");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLoginAt(now);

        return users.save(user).flatMap(savedUser -> {
            UserIdentityEntity identity = new UserIdentityEntity();
            identity.setUserId(savedUser.getId());
            identity.setProvider(GOOGLE);
            identity.setProviderSubject(request.getGoogleSubject());
            identity.setProviderEmail(request.getEmail());
            identity.setEmailVerified(request.isEmailVerified());
            identity.setCreatedAt(now);
            identity.setLastLoginAt(now);

            return identities.save(identity)
                    .flatMap(savedIdentity -> roles.findByName(CUSTOMER)
                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                    "The CUSTOMER role has not been seeded")))
                            .flatMap(role -> roleAssignments.assign(savedUser.getId(), role.getId()))
                            .then(resolveAccount(savedUser, savedIdentity.getId())));
        });
    }

    private Mono<ResolvedAccount> resolveAccount(UserEntity user, UUID identityId) {
        return roleAssignments.findRoleNames(user.getId())
                .map(role -> "ROLE_" + role)
                .collectList()
                .map(roleNames -> new ResolvedAccount(
                        userMapper.toResponse(user, identityId, roleNames), identityId));
    }

    private Mono<TokenPairResponse> createSession(
            ResolvedAccount account,
            GoogleLoginRequest request) {
        Instant now = Instant.now();
        AuthSessionEntity session = new AuthSessionEntity();
        session.setUserId(account.user().getId());
        session.setIdentityId(account.identityId());
        session.setProvider(GOOGLE);
        session.setIpAddress(parseIp(request.getIpAddress()));
        session.setUserAgent(request.getUserAgent());
        session.setCreatedAt(now);
        session.setLastUsedAt(now);
        session.setExpiresAt(now.plus(jwtTokens.refreshLifetime()));

        return sessions.save(session).flatMap(savedSession -> {
            IssuedTokenPair issued = jwtTokens.issue(account.user(), savedSession.getId());
            return saveRefreshToken(savedSession.getId(), null, issued)
                    .thenReturn(toResponse(issued, account.user()));
        });
    }

    private Mono<TokenPairResponse> rotate(Jwt jwt, String rawRefreshToken) {
        UUID jwtId = UUID.fromString(jwt.getId());
        UUID sessionId = requiredUuidClaim(jwt, "sid");
        UUID userId = UUID.fromString(jwt.getSubject());
        Instant now = Instant.now();

        return refreshTokens.findForUpdateByJwtId(jwtId)
                .switchIfEmpty(Mono.error(new InvalidSessionException("Refresh session was not found")))
                .flatMap(stored -> sessions.findById(stored.getSessionId())
                        .switchIfEmpty(Mono.error(new InvalidSessionException("Authentication session was not found")))
                        .flatMap(session -> {
                            if (!stored.getSessionId().equals(sessionId)
                                    || !session.getUserId().equals(userId)
                                    || !hashMatches(stored.getTokenHash(), rawRefreshToken)
                                    || stored.getUsedAt() != null
                                    || stored.getRevokedAt() != null) {
                                return Mono.error(new RefreshTokenReuseException(session.getId()));
                            }
                            if (session.getRevokedAt() != null
                                    || !session.getExpiresAt().isAfter(now)
                                    || !stored.getExpiresAt().isAfter(now)) {
                                return Mono.error(new InvalidSessionException("Authentication session has expired"));
                            }

                            return userService.getById(userId).flatMap(user -> {
                                stored.setUsedAt(now);
                                session.setLastUsedAt(now);
                                IssuedTokenPair issued = jwtTokens.issue(user, session.getId());
                                return refreshTokens.save(stored)
                                        .then(sessions.save(session))
                                        .then(saveRefreshToken(session.getId(), stored.getId(), issued))
                                        .thenReturn(toResponse(issued, user));
                            });
                        }));
    }

    private Mono<RefreshTokenEntity> saveRefreshToken(
            UUID sessionId,
            UUID parentTokenId,
            IssuedTokenPair issued) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setSessionId(sessionId);
        entity.setJwtId(issued.refreshJwtId());
        entity.setTokenHash(tokenHashes.sha256(issued.refreshToken()));
        entity.setParentTokenId(parentTokenId);
        entity.setIssuedAt(Instant.now());
        entity.setExpiresAt(issued.refreshExpiresAt());
        return refreshTokens.save(entity);
    }

    private Mono<Void> revokeSession(UUID sessionId, String reason) {
        return sessions.findById(sessionId)
                .flatMap(session -> {
                    session.setRevokedAt(Instant.now());
                    session.setRevokeReason(reason);
                    return sessions.save(session);
                })
                .then(refreshTokens.revokeActiveForSession(sessionId, reason))
                .then();
    }

    private TokenPairResponse toResponse(IssuedTokenPair issued, AccountUserResponse user) {
        return new TokenPairResponse(
                issued.accessToken(),
                issued.refreshToken(),
                jwtTokens.accessLifetime().toSeconds(),
                jwtTokens.refreshLifetime().toSeconds(),
                user);
    }

    private boolean hashMatches(String storedHash, String rawToken) {
        byte[] expected = storedHash.getBytes(StandardCharsets.US_ASCII);
        byte[] actual = tokenHashes.sha256(rawToken).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }

    private UUID requiredUuidClaim(Jwt jwt, String name) {
        String value = jwt.getClaimAsString(name);
        if (value == null) {
            throw new InvalidSessionException("Refresh token is missing " + name);
        }
        try {
            return UUID.fromString(value);
        }
        catch (IllegalArgumentException exception) {
            throw new InvalidSessionException("Refresh token contains an invalid " + name);
        }
    }

    private InetAddress parseIp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String firstAddress = value.split(",", 2)[0].trim();
        try {
            return InetAddress.getByName(firstAddress);
        }
        catch (UnknownHostException exception) {
            return null;
        }
    }

    private record ResolvedAccount(AccountUserResponse user, UUID identityId) {
    }

    private static final class RefreshTokenReuseException extends RuntimeException {
        private final UUID sessionId;

        private RefreshTokenReuseException(UUID sessionId) {
            this.sessionId = sessionId;
        }

        private UUID sessionId() {
            return sessionId;
        }
    }
}
