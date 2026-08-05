package com.munchy.account.mapper;

import com.munchy.account.config.ModelMapperConfig;
import com.munchy.account.dto.address.CreateAddressRequest;
import com.munchy.account.dto.address.UpdateAddressRequest;
import com.munchy.account.entity.UserAddressEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AddressMapperTests {
    private final AddressMapper mapper = new AddressMapper(new ModelMapperConfig().modelMapper());

    @Test
    void mapsClientFieldsButDoesNotCreateServerControlledFields() {
        CreateAddressRequest request = addressRequest(new CreateAddressRequest());

        UserAddressEntity entity = mapper.toEntity(request);

        assertThat(entity.getCity()).isEqualTo("Kolkata");
        assertThat(entity.getRecipientName()).isEqualTo("Munchy User");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getUserId()).isNull();
        assertThat(entity.isDefaultAddress()).isFalse();
        assertThat(entity.isActive()).isFalse();
    }

    @Test
    void updateCannotOverwriteOwnershipOrLifecycleFields() {
        UserAddressEntity entity = new UserAddressEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(60);
        entity.setId(id);
        entity.setUserId(userId);
        entity.setDefaultAddress(true);
        entity.setActive(true);
        entity.setCreatedAt(createdAt);

        UpdateAddressRequest request = addressRequest(new UpdateAddressRequest());
        mapper.updateEntity(request, entity);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.isDefaultAddress()).isTrue();
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getCity()).isEqualTo("Kolkata");
    }

    private <T extends CreateAddressRequest> T addressRequest(T request) {
        request.setLabel("HOME");
        request.setRecipientName("Munchy User");
        request.setRecipientPhone("+919999999999");
        request.setAddressLine1("Flat 3B");
        request.setLocality("Salt Lake");
        request.setCity("Kolkata");
        request.setState("West Bengal");
        request.setPostalCode("700091");
        request.setCountryCode("IN");
        return request;
    }
}
