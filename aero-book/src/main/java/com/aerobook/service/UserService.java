package com.aerobook.service;

import com.aerobook.enitity.Role;
import com.aerobook.enitity.User;
import com.aerobook.domain.dto.request.RoleAssignRequest;
import com.aerobook.domain.dto.request.get.UserGetRequest;
import com.aerobook.domain.dto.request.UserStatusRequest;
import com.aerobook.domain.dto.request.UserUpdateRequest;
import com.aerobook.domain.dto.response.UserResponse;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.UserMapper;
import com.aerobook.repository.RoleRepository;
import com.aerobook.repository.UserRepository;
import com.aerobook.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper     userMapper;

    // ----------------------------------------------------------------
    // Get users — spec + pageable
    // ----------------------------------------------------------------
    public List<UserResponse> getUsers(UserGetRequest request, Pageable pageable) {
        return userRepository.findAll(request.toSpecification(), pageable)
                .map(userMapper::toResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Get user by id
    // ----------------------------------------------------------------
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    // ----------------------------------------------------------------
    // Get own profile — from SecurityContext
    // ----------------------------------------------------------------
    public UserResponse getMyProfile() {
        Long userId = resolveCurrentUserId();
        return userMapper.toResponse(findUserById(userId));
    }

    // ----------------------------------------------------------------
    // Update own profile
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse updateMyProfile(UserUpdateRequest request) {
        Long userId = resolveCurrentUserId();
        User user   = findUserById(userId);
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(userRepository.save(user));
    }

    // ----------------------------------------------------------------
    // Update any user — SUPER_ADMIN only
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserById(id);
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(userRepository.save(user));
    }

    // ----------------------------------------------------------------
    // Assign role — SUPER_ADMIN only
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse assignRole(Long userId, RoleAssignRequest request) {
        User user = findUserById(userId);
        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role", "name", request.role().name()));

        user.getRoles().add(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    // ----------------------------------------------------------------
    // Remove role — SUPER_ADMIN only
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse removeRole(Long userId, RoleAssignRequest request) {
        User user = findUserById(userId);

        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(request.role()));

        if (!hasRole) {
            throw new AeroBookException(
                    "User does not have role: " + request.role(),
                    HttpStatus.BAD_REQUEST,
                    "ROLE_NOT_ASSIGNED"
            );
        }

        if (user.getRoles().size() == 1) {
            throw new AeroBookException(
                    "Cannot remove last role from user",
                    HttpStatus.BAD_REQUEST,
                    "CANNOT_REMOVE_LAST_ROLE"
            );
        }

        user.getRoles().removeIf(r -> r.getName().equals(request.role()));
        return userMapper.toResponse(userRepository.save(user));
    }

    // ----------------------------------------------------------------
    // Update status — SUPER_ADMIN only
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse updateStatus(Long userId, UserStatusRequest request) {
        User user = findUserById(userId);
        user.setStatus(request.status());
        return userMapper.toResponse(userRepository.save(user));
    }

    // ----------------------------------------------------------------
    // Delete user — SUPER_ADMIN only
    // ----------------------------------------------------------------
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    // ----------------------------------------------------------------
    // Internal helper — used across services
    // ----------------------------------------------------------------
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // ----------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------
    private Long resolveCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return principal.getId();
    }
}
