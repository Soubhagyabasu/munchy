package com.munchy.account.mapper;

import com.munchy.account.dto.auth.GoogleLoginRequest;
import com.munchy.account.dto.user.AccountUserResponse;
import com.munchy.account.entity.UserEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserMapper {
    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public UserEntity fromGoogle(GoogleLoginRequest request) {
        UserEntity entity = modelMapper.map(request, UserEntity.class);
        entity.setPictureUrl(request.getPictureUrl());
        return entity;
    }

    public AccountUserResponse toResponse(UserEntity entity, UUID identityId, List<String> roles) {
        AccountUserResponse response = modelMapper.map(entity, AccountUserResponse.class);
        response.setIdentityId(identityId);
        response.setRoles(roles);
        return response;
    }
}
