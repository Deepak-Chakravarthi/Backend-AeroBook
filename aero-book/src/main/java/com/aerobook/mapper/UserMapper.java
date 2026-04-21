package com.aerobook.mapper;

import com.aerobook.domain.dto.request.UserUpdateRequest;
import com.aerobook.entity.Role;
import com.aerobook.entity.User;
import com.aerobook.domain.dto.request.RegisterRequest;
import com.aerobook.domain.dto.response.UserResponse;
import com.aerobook.domain.enums.RoleType;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The interface User mapper.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * To entity user.
     *
     * @param request the request
     * @return the user
     */
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "password",  ignore = true)   // encoded separately
    @Mapping(target = "status",    ignore = true)   // set in service
    @Mapping(target = "roles",     ignore = true)   // assigned in service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);

    /**
     * Update entity.
     *
     * @param request the request
     * @param user    the user
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "username",  ignore = true)   // username not updatable
    @Mapping(target = "email",     ignore = true)   // email not updatable
    @Mapping(target = "password",  ignore = true)
    @Mapping(target = "status",    ignore = true)
    @Mapping(target = "roles",     ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);

    /**
     * To response user response.
     *
     * @param user the user
     * @return the user response
     */
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    /**
     * Map roles set.
     *
     * @param roles the roles
     * @return the set
     */
    default Set<RoleType> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
