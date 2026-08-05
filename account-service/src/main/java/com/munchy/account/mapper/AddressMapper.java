package com.munchy.account.mapper;

import com.munchy.account.dto.address.AddressResponse;
import com.munchy.account.dto.address.CreateAddressRequest;
import com.munchy.account.dto.address.UpdateAddressRequest;
import com.munchy.account.entity.UserAddressEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    private final ModelMapper modelMapper;

    public AddressMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public UserAddressEntity toEntity(CreateAddressRequest request) {
        return modelMapper.map(request, UserAddressEntity.class);
    }

    public void updateEntity(UpdateAddressRequest request, UserAddressEntity entity) {
        modelMapper.map(request, entity);
    }

    public AddressResponse toResponse(UserAddressEntity entity) {
        return modelMapper.map(entity, AddressResponse.class);
    }
}
