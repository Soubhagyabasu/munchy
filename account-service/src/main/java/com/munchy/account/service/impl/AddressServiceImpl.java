package com.munchy.account.service.impl;

import com.munchy.account.dto.address.AddressResponse;
import com.munchy.account.dto.address.CreateAddressRequest;
import com.munchy.account.dto.address.UpdateAddressRequest;
import com.munchy.account.entity.UserAddressEntity;
import com.munchy.account.exception.ResourceNotFoundException;
import com.munchy.account.mapper.AddressMapper;
import com.munchy.account.repository.UserAddressRepository;
import com.munchy.account.repository.UserRepository;
import com.munchy.account.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AddressServiceImpl implements AddressService {
    private final UserAddressRepository addresses;
    private final UserRepository users;
    private final AddressMapper mapper;
    private final TransactionalOperator transactions;

    public AddressServiceImpl(
            UserAddressRepository addresses,
            UserRepository users,
            AddressMapper mapper,
            TransactionalOperator transactions) {
        this.addresses = addresses;
        this.users = users;
        this.mapper = mapper;
        this.transactions = transactions;
    }

    @Override
    public Flux<AddressResponse> getAll(UUID userId) {
        return addresses.findByUserIdAndActiveTrueAndDeletedAtIsNullOrderByUpdatedAtDesc(userId)
                .map(mapper::toResponse);
    }

    @Override
    public Mono<AddressResponse> create(UUID userId, CreateAddressRequest request) {
        validateCoordinates(request.getLatitude(), request.getLongitude());
        Mono<AddressResponse> operation = users.existsById(userId)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Munchy user was not found")))
                .then(addresses.findFirstByUserIdAndDefaultAddressTrueAndActiveTrueAndDeletedAtIsNull(userId)
                        .hasElement())
                .flatMap(hasDefault -> {
                    Instant now = Instant.now();
                    UserAddressEntity entity = mapper.toEntity(request);
                    entity.setUserId(userId);
                    entity.setCountryCode(request.getCountryCode().toUpperCase(Locale.ROOT));
                    entity.setDefaultAddress(!hasDefault);
                    entity.setActive(true);
                    entity.setCreatedAt(now);
                    entity.setUpdatedAt(now);
                    return addresses.save(entity);
                })
                .map(mapper::toResponse);
        return transactions.transactional(operation);
    }

    @Override
    public Mono<AddressResponse> update(UUID userId, UUID addressId, UpdateAddressRequest request) {
        validateCoordinates(request.getLatitude(), request.getLongitude());
        return requireOwnedAddress(userId, addressId)
                .flatMap(entity -> {
                    mapper.updateEntity(request, entity);
                    entity.setCountryCode(request.getCountryCode().toUpperCase(Locale.ROOT));
                    entity.setUpdatedAt(Instant.now());
                    return addresses.save(entity);
                })
                .map(mapper::toResponse);
    }

    @Override
    public Mono<AddressResponse> setDefault(UUID userId, UUID addressId) {
        Mono<AddressResponse> operation = requireOwnedAddress(userId, addressId)
                .flatMap(entity -> addresses.clearDefaultForUser(userId)
                        .then(Mono.defer(() -> {
                            entity.setDefaultAddress(true);
                            entity.setUpdatedAt(Instant.now());
                            return addresses.save(entity);
                        })))
                .map(mapper::toResponse);
        return transactions.transactional(operation);
    }

    @Override
    public Mono<Void> delete(UUID userId, UUID addressId) {
        Mono<Void> operation = requireOwnedAddress(userId, addressId)
                .flatMap(entity -> {
                    boolean wasDefault = entity.isDefaultAddress();
                    entity.setDefaultAddress(false);
                    entity.setActive(false);
                    entity.setDeletedAt(Instant.now());
                    entity.setUpdatedAt(Instant.now());
                    return addresses.save(entity)
                            .then(wasDefault ? selectReplacementDefault(userId) : Mono.empty());
                })
                .then();
        return transactions.transactional(operation);
    }

    private Mono<Void> selectReplacementDefault(UUID userId) {
        return addresses.findFirstByUserIdAndActiveTrueAndDeletedAtIsNullOrderByUpdatedAtDesc(userId)
                .flatMap(entity -> {
                    entity.setDefaultAddress(true);
                    entity.setUpdatedAt(Instant.now());
                    return addresses.save(entity);
                })
                .then();
    }

    private Mono<UserAddressEntity> requireOwnedAddress(UUID userId, UUID addressId) {
        return addresses.findByIdAndUserIdAndActiveTrueAndDeletedAtIsNull(addressId, userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Address was not found")));
    }

    private void validateCoordinates(Object latitude, Object longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Latitude and longitude must be supplied together");
        }
    }
}
