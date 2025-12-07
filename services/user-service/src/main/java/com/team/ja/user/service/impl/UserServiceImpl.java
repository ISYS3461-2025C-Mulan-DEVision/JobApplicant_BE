package com.team.ja.user.service.impl;

import com.team.ja.common.enumeration.ApplicationStatus;
import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.dto.response.UserResponse;
import com.team.ja.user.mapper.CountryMapper;
import com.team.ja.user.mapper.UserMapper;
import com.team.ja.user.model.User;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of UserService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final UserMapper userMapper;
    private final CountryMapper countryMapper;

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all active users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(User::isActive)
                .map(this::mapUserWithCountry)
                .toList();
    }

    @Override
    public UserResponse getUserById(UUID id) {
        log.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));
        return mapUserWithCountry(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new NotFoundException("User", "email", email));
        return mapUserWithCountry(user);
    }

    /**
     * Map User entity to UserResponse with nested Country.
     */
    private UserResponse mapUserWithCountry(User user) {
        UserResponse response = userMapper.toResponse(user);
        
        // Fetch and set country if exists
        if (user.getCountryId() != null) {
            countryRepository.findById(user.getCountryId())
                    .map(countryMapper::toResponse)
                    .ifPresent(response::setCountry);
        }
        
        return response;
    }
}

