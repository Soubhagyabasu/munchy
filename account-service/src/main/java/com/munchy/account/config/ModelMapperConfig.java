package com.munchy.account.config;

import com.munchy.account.dto.address.CreateAddressRequest;
import com.munchy.account.dto.address.UpdateAddressRequest;
import com.munchy.account.entity.UserAddressEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        mapper.createTypeMap(CreateAddressRequest.class, UserAddressEntity.class)
                .addMappings(mapping -> {
                    mapping.skip(UserAddressEntity::setId);
                    mapping.skip(UserAddressEntity::setUserId);
                    mapping.skip(UserAddressEntity::setDefaultAddress);
                    mapping.skip(UserAddressEntity::setActive);
                    mapping.skip(UserAddressEntity::setCreatedAt);
                    mapping.skip(UserAddressEntity::setUpdatedAt);
                    mapping.skip(UserAddressEntity::setDeletedAt);
                });
        mapper.createTypeMap(UpdateAddressRequest.class, UserAddressEntity.class)
                .addMappings(mapping -> {
                    mapping.skip(UserAddressEntity::setId);
                    mapping.skip(UserAddressEntity::setUserId);
                    mapping.skip(UserAddressEntity::setDefaultAddress);
                    mapping.skip(UserAddressEntity::setActive);
                    mapping.skip(UserAddressEntity::setCreatedAt);
                    mapping.skip(UserAddressEntity::setUpdatedAt);
                    mapping.skip(UserAddressEntity::setDeletedAt);
                });
        return mapper;
    }
}
