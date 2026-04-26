package com.aerobook.controller;

import com.aerobook.annotations.ExemptAuthorization;
import com.aerobook.constants.ApiConstants;
import com.aerobook.domain.dto.request.LoginRequest;
import com.aerobook.domain.dto.request.RegisterRequest;
import com.aerobook.domain.dto.response.AuthResponse;
import com.aerobook.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The type Auth controller.
 */
@RestController
@RequestMapping(ApiConstants.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/register")
    @ExemptAuthorization(reason = "Public registration endpoint")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Login response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/login")
    @ExemptAuthorization(reason = "Public login endpoint")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
