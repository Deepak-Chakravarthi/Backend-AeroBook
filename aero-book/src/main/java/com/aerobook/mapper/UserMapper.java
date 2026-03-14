package com.aerobook.mapper;

import com.aerobook.enitity.Role;
import com.aerobook.enitity.User;
import com.aerobook.domain.dto.request.RegisterRequest;
import com.aerobook.domain.dto.response.UserResponse;
import com.aerobook.domain.enums.RoleType;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "password",  ignore = true)   // encoded separately
    @Mapping(target = "status",    ignore = true)   // set in service
    @Mapping(target = "roles",     ignore = true)   // assigned in service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    default Set<RoleType> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
