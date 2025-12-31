package com.team.ja.auth.service.impl;

import com.team.ja.auth.dto.request.LoginRequest;
import com.team.ja.auth.dto.request.RefreshTokenRequest;
import com.team.ja.auth.dto.request.RegisterRequest;
import com.team.ja.auth.dto.response.AuthResponse;
import com.team.ja.auth.kafka.UserRegisteredProducer;
import com.team.ja.auth.model.AuthCredential;
import com.team.ja.auth.model.VerificationToken;
import com.team.ja.auth.repository.AuthCredentialRepository;
import com.team.ja.auth.repository.VerificationTokenRepository;
import com.team.ja.auth.security.JwtService;
import com.team.ja.auth.service.AuthService;
import com.team.ja.auth.service.EmailService;
import com.team.ja.auth.service.TokenBlacklistService;
import com.team.ja.common.enumeration.Role;
import com.team.ja.common.event.UserRegisteredEvent;
import com.team.ja.common.exception.BadRequestException;
import com.team.ja.common.exception.ConflictException;
import com.team.ja.common.exception.NotFoundException;
import com.team.ja.common.exception.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Implementation of AuthService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthCredentialRepository authCredentialRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRegisteredProducer userRegisteredProducer;
    private final EmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (authCredentialRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                "Email already registered: " + request.getEmail()
            );
        }

        // Create auth credential (inactive and unverified initially)
        AuthCredential credential = AuthCredential.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(Role.FREE)
            .isActive(false) // Account is inactive until email is verified
            .emailVerified(false) // Email not verified yet
            .build();

        AuthCredential savedCredential = authCredentialRepository.save(
            credential
        );
        savedCredential.setUserId(savedCredential.getId()); // Align userId with primary key
        authCredentialRepository.save(savedCredential);
        log.info(
            "Created inactive auth credential for: {}",
            request.getEmail()
        );

        // Generate and save verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
            .token(token)
            .credential(savedCredential)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .countryAbbreviation(request.getCountry())
            .phone(request.getPhone())
            .address(request.getAddress())
            .city(request.getCity())
            .expiryDate(LocalDateTime.now().plusHours(24)) // Token valid for 24 hours
            .build();
        verificationTokenRepository.save(verificationToken);
        log.info(
            "Generated verification token for {}: {}",
            request.getEmail(),
            token
        );

        // Send activation email
        emailService.sendActivationEmail(savedCredential, token);

        log.info(
            "User registration initiated. Activation email sent to: {}",
            request.getEmail()
        );
    }

    @Override
    @Transactional
    public AuthResponse activateAccount(String token) {
        log.info("Activating account with token: {}", token);

        VerificationToken verificationToken = verificationTokenRepository
            .findByToken(token)
            .orElseThrow(() ->
                new NotFoundException("Invalid activation token")
            );

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken); // Clean up expired token
            throw new BadRequestException("Activation token has expired");
        }

        AuthCredential credential = verificationToken.getCredential();

        if (credential.isActive() && credential.isEmailVerified()) {
            // Account already active, clean up token
            verificationTokenRepository.delete(verificationToken);
            log.warn(
                "Account for {} is already active.",
                credential.getEmail()
            );
            // Proceed to log in the user if token was valid but account already active
            return generateAuthResponse(credential);
        }

        credential.activate(); // Set isActive to true
        credential.setEmailVerified(true);
        AuthCredential activatedCredential = authCredentialRepository.save(
            credential
        );
        log.info(
            "Account activated for: {} with name {} {}",
            activatedCredential.getEmail(),
            verificationToken.getFirstName(),
            verificationToken.getLastName()
        );

        // Publish Kafka event for user-service to create user profile NOW
        UserRegisteredEvent event = UserRegisteredEvent.builder()
            .userId(activatedCredential.getId())
            .email(activatedCredential.getEmail())
            .firstName(verificationToken.getFirstName())
            .lastName(verificationToken.getLastName())
            .countryAbbreviation(verificationToken.getCountryAbbreviation())
            .phone(verificationToken.getPhone())
            .address(verificationToken.getAddress())
            .city(verificationToken.getCity())
            .build();
        userRegisteredProducer.sendUserRegisteredEvent(event);
        log.info(
            "UserRegisteredEvent published for userId: {}",
            activatedCredential.getId()
        );

        // Delete the used verification token
        verificationTokenRepository.delete(verificationToken);
        log.info("Verification token deleted for {}", credential.getEmail());

        // Generate tokens for immediate login
        return generateAuthResponse(activatedCredential);
    }

    @Override
    @Transactional
    public void resendActivationEmail(String email) {
        log.info("Resending activation email for: {}", email);

        AuthCredential credential = authCredentialRepository
            .findByEmail(email)
            .orElseThrow(() ->
                new NotFoundException("No account registered with this email")
            );

        if (credential.isActive() && credential.isEmailVerified()) {
            log.info("Account already active for {}. Skipping resend.", email);
            return;
        }

        // Invalidate all old tokens for this user before creating a new one
        verificationTokenRepository.deleteByCredential(credential);
        log.info("Invalidated old verification tokens for {}", email);

        // Generate and save a new verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
            .token(token)
            .credential(credential)
            .expiryDate(LocalDateTime.now().plusHours(24)) // Token valid for 24 hours
            .build();
        verificationTokenRepository.save(verificationToken);
        log.info("Generated new verification token for {}: {}", email, token);

        // Send activation email
        emailService.sendActivationEmail(credential, token);

        log.info("Resent activation email to: {}", email);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        AuthCredential credential = authCredentialRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() ->
                new UnauthorizedException("Invalid email or password")
            );

        // Check if account is active (email verified)
        if (!credential.isActive()) {
            throw new UnauthorizedException(
                "Account is not active. Please verify your email."
            );
        }

        // Check if account is locked
        if (!credential.isAccountNonLocked()) {
            throw new UnauthorizedException(
                "Account is locked. Please try again later."
            );
        }

        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            // Record failed attempt
            credential.recordFailedLogin();
            authCredentialRepository.save(credential);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Record successful login
        credential.recordSuccessfulLogin();
        authCredentialRepository.save(credential);

        log.info("User logged in successfully: {}", request.getEmail());

        // Generate tokens
        return generateAuthResponse(credential);
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
        AuthCredential credential = authCredentialRepository
            .findByEmailAndIsActiveTrue(email)
            .orElseThrow(() ->
                new UnauthorizedException("User not found or inactive")
            );

        // Validate token
        if (!jwtService.isTokenValid(refreshToken, credential)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(
            credential,
            credential.getId(),
            credential.getRole().name()
        );

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
            AuthCredential credential = authCredentialRepository
                .findByEmailAndIsActiveTrue(email)
                .orElse(null);
            return (
                credential != null && jwtService.isTokenValid(token, credential)
            );
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            String jti = jwtService.extractJti(token);
            Date expiration = jwtService.extractExpiration(token);
            tokenBlacklistService.blacklistToken(jti, expiration);
            log.info("Token {} has been blacklisted.", jti);
        } catch (Exception e) {
            log.warn("Could not blacklist token: {}", e.getMessage());
        }
    }

    private AuthResponse generateAuthResponse(AuthCredential credential) {
        String accessToken = jwtService.generateAccessToken(
            credential,
            credential.getId(),
            credential.getRole().name()
        );
        String refreshToken = jwtService.generateRefreshToken(credential);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(accessTokenExpiration / 1000)
            .userId(credential.getId())
            .email(credential.getEmail())
            .role(credential.getRole().name())
            .build();
    }
}
