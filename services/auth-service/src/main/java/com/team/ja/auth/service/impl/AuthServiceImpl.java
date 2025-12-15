package com.team.ja.auth.service.impl;

import com.team.ja.auth.dto.request.LoginRequest;
import com.team.ja.auth.dto.request.RefreshTokenRequest;
import com.team.ja.auth.dto.request.RegisterRequest;
import com.team.ja.auth.dto.response.AuthResponse;
import com.team.ja.auth.kafka.UserRegisteredProducer;
import com.team.ja.auth.model.AuthCredential;
import com.team.ja.auth.repository.AuthCredentialRepository;
import com.team.ja.auth.security.JwtService;
import com.team.ja.auth.service.AuthService;
import com.team.ja.common.enumeration.Role;
import com.team.ja.common.event.UserRegisteredEvent;
import com.team.ja.common.exception.BadRequestException;
import com.team.ja.common.exception.ConflictException;
import com.team.ja.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthCredentialRepository authCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRegisteredProducer userRegisteredProducer;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (authCredentialRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        // Create auth credential
        AuthCredential credential = AuthCredential.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.FREE)
                .build();

        AuthCredential savedCredential = authCredentialRepository.save(credential);
        // Align userId with primary key so downstream services share the same UUID
        savedCredential.setUserId(savedCredential.getId());
        savedCredential = authCredentialRepository.save(savedCredential);
        log.info("Created auth credential for: {}", request.getEmail());

        // Publish Kafka event for user-service to create user profile
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedCredential.getId())  // Use credential ID as user ID
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        userRegisteredProducer.sendUserRegisteredEvent(event);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(
                savedCredential,
                savedCredential.getId(),
                savedCredential.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(savedCredential);

        log.info("User registered successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .userId(savedCredential.getId())
                .email(savedCredential.getEmail())
                .role(savedCredential.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            // Record failed attempt
            authCredentialRepository.findByEmail(request.getEmail())
                    .ifPresent(credential -> {
                        credential.recordFailedLogin();
                        authCredentialRepository.save(credential);
                    });
            throw new UnauthorizedException("Invalid email or password");
        }

        // Get credential
        AuthCredential credential = authCredentialRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Check if account is locked
        if (!credential.isAccountNonLocked()) {
            throw new UnauthorizedException("Account is locked. Please try again later.");
        }

        // Record successful login
        credential.recordSuccessfulLogin();
        authCredentialRepository.save(credential);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(
                credential,
                credential.getId(),
                credential.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(credential);

        log.info("User logged in successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiration / 1000)
                .userId(credential.getId())
                .email(credential.getEmail())
                .role(credential.getRole().name())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        // Extract email from token
        String email = jwtService.extractUsername(refreshToken);

        // Get credential
        AuthCredential credential = authCredentialRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Validate token
        if (!jwtService.isTokenValid(refreshToken, credential)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(
                credential,
                credential.getId(),
                credential.getRole().name());

        log.info("Access token refreshed for: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .expiresIn(accessTokenExpiration / 1000)
                .userId(credential.getId())
                .email(credential.getEmail())
                .role(credential.getRole().name())
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String email = jwtService.extractUsername(token);
            AuthCredential credential = authCredentialRepository.findByEmailAndIsActiveTrue(email)
                    .orElse(null);
            return credential != null && jwtService.isTokenValid(token, credential);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
