package com.munchy.account.mapper;

import com.munchy.account.dto.location.CurrentLocationRequest;
import com.munchy.account.dto.location.CurrentLocationResponse;
import com.munchy.account.entity.SessionLocationEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    private final ModelMapper modelMapper;

    public LocationMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public void updateEntity(CurrentLocationRequest request, SessionLocationEntity entity) {
        modelMapper.map(request, entity);
    }

    public CurrentLocationResponse toResponse(SessionLocationEntity entity) {
        return modelMapper.map(entity, CurrentLocationResponse.class);
    }
}
