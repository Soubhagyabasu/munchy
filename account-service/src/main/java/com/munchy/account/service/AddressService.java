package com.munchy.account.service;

import com.munchy.account.dto.address.AddressResponse;
import com.munchy.account.dto.address.CreateAddressRequest;
import com.munchy.account.dto.address.UpdateAddressRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AddressService {
    Flux<AddressResponse> getAll(UUID userId);
    Mono<AddressResponse> create(UUID userId, CreateAddressRequest request);
    Mono<AddressResponse> update(UUID userId, UUID addressId, UpdateAddressRequest request);
    Mono<AddressResponse> setDefault(UUID userId, UUID addressId);
    Mono<Void> delete(UUID userId, UUID addressId);
}
