package com.munchy.account.controller;

import com.munchy.account.dto.address.AddressResponse;
import com.munchy.account.dto.address.CreateAddressRequest;
import com.munchy.account.dto.address.UpdateAddressRequest;
import com.munchy.account.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
public class AddressController {
    private final AddressService addresses;

    public AddressController(AddressService addresses) {
        this.addresses = addresses;
    }

    @GetMapping
    public Flux<AddressResponse> getAll(@RequestHeader("X-User-Id") UUID userId) {
        return addresses.getAll(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AddressResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateAddressRequest request) {
        return addresses.create(userId, request);
    }

    @PutMapping("/{addressId}")
    public Mono<AddressResponse> update(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        return addresses.update(userId, addressId, request);
    }

    @PutMapping("/{addressId}/default")
    public Mono<AddressResponse> setDefault(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId) {
        return addresses.setDefault(userId, addressId);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId) {
        return addresses.delete(userId, addressId);
    }
}
