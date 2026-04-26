package com.aerobook.controller;

import com.aerobook.annotations.AuthenticatedEndpoint;
import com.aerobook.constants.ApiConstants;
import com.aerobook.domain.dto.request.RoleAssignRequest;
import com.aerobook.domain.dto.request.UserStatusRequest;
import com.aerobook.domain.dto.request.UserUpdateRequest;
import com.aerobook.domain.dto.request.get.UserGetRequest;
import com.aerobook.domain.dto.response.UserResponse;
import com.aerobook.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type User controller.
 */
@RestController
@RequestMapping(ApiConstants.USERS)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Gets users.
     *
     * @param id        the id
     * @param username  the username
     * @param email     the email
     * @param firstName the first name
     * @param lastName  the last name
     * @param status    the status
     * @param role      the role
     * @param pageable  the pageable
     * @return the users
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            Pageable pageable) {

        UserGetRequest request = UserGetRequest.builder()
                .id(id).username(username).email(email)
                .firstName(firstName).lastName(lastName)
                .status(status).role(role)
                .build();

        return ResponseEntity.ok(userService.getUsers(request, pageable));
    }

    /**
     * Gets user by id.
     *
     * @param id the id
     * @return the user by id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Gets my profile.
     *
     * @return the my profile
     */
    @GetMapping("/me")
    @AuthenticatedEndpoint
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    /**
     * Update my profile response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PutMapping("/me")
    @AuthenticatedEndpoint
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    /**
     * Update user response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Assign role response entity.
     *
     * @param userId  the user id
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleAssignRequest request) {
        return ResponseEntity.ok(userService.assignRole(userId, request));
    }

    /**
     * Remove role response entity.
     *
     * @param userId  the user id
     * @param request the request
     * @return the response entity
     */
    @DeleteMapping("/{userId}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleAssignRequest request) {
        return ResponseEntity.ok(userService.removeRole(userId, request));
    }

    /**
     * Update status response entity.
     *
     * @param userId  the user id
     * @param request the request
     * @return the response entity
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(userService.updateStatus(userId, request));
    }

    /**
     * Delete user response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}