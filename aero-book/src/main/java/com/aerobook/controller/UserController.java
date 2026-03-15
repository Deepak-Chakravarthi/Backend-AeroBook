package com.aerobook.controller;

import com.aerobook.annotations.AuthenticatedEndpoint;
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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    @AuthenticatedEndpoint
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping("/me")
    @AuthenticatedEndpoint
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleAssignRequest request) {
        return ResponseEntity.ok(userService.assignRole(userId, request));
    }

    @DeleteMapping("/{userId}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleAssignRequest request) {
        return ResponseEntity.ok(userService.removeRole(userId, request));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(userService.updateStatus(userId, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}