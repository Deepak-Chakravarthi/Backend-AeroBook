package com.aerobook.service;

import com.aerobook.domain.dto.request.LoginRequest;
import com.aerobook.domain.dto.request.RegisterRequest;
import com.aerobook.domain.dto.response.AuthResponse;
import com.aerobook.domain.enums.RoleType;
import com.aerobook.domain.enums.UserStatus;
import com.aerobook.enitity.Role;
import com.aerobook.enitity.User;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.UserMapper;
import com.aerobook.repository.RoleRepository;
import com.aerobook.repository.UserRepository;
import com.aerobook.security.JwtTokenProvider;
import com.aerobook.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role", "name", request.role().name()));

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(role));

        User saved = userRepository.save(user);
        UserPrincipal principal = UserPrincipal.of(saved);
        String token = jwtTokenProvider.generateToken(principal);

        return buildAuthResponse(saved, token);
    }


    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmailWithRoles(request.email())
                .orElseThrow(() -> new AeroBookException(
                        "User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        return buildAuthResponse(user, token);
    }


    private AuthResponse buildAuthResponse(User user, String token) {
        Set<RoleType> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponse.of(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                token,
                jwtTokenProvider.getExpirationMs()
        );
    }
}