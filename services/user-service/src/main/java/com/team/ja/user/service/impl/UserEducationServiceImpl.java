package com.team.ja.user.service.impl;

import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.dto.request.CreateUserEducationRequest;
import com.team.ja.user.dto.request.UpdateUserEducationRequest;
import com.team.ja.user.dto.response.UserEducationResponse;
import com.team.ja.user.mapper.UserEducationMapper;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.service.UserEducationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of UserEducationService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserEducationServiceImpl implements UserEducationService {

    private final UserEducationRepository userEducationRepository;
    private final UserRepository userRepository;
    private final UserEducationMapper userEducationMapper;

    @Override
    @Transactional
    public UserEducationResponse createEducation(UUID userId, CreateUserEducationRequest request) {
        log.info("Creating education for user: {}", userId);
        
        // Verify user exists
        validateUserExists(userId);
        
        UserEducation education = UserEducation.builder()
                .userId(userId)
                .institution(request.getInstitution())
                .educationLevel(request.getEducationLevel())
                .fieldOfStudy(request.getFieldOfStudy())
                .degree(request.getDegree())
                .gpa(request.getGpa())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();
        
        UserEducation saved = userEducationRepository.save(education);
        log.info("Created education {} for user {}", saved.getId(), userId);
        
        return userEducationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserEducationResponse updateEducation(UUID userId, UUID educationId, UpdateUserEducationRequest request) {
        log.info("Updating education {} for user {}", educationId, userId);
        
        UserEducation education = userEducationRepository.findByIdAndUserIdAndIsActiveTrue(educationId, userId)
                .orElseThrow(() -> new NotFoundException("Education", "id", educationId.toString()));
        
        // Update fields if provided
        if (request.getInstitution() != null) {
            education.setInstitution(request.getInstitution());
        }
        if (request.getEducationLevel() != null) {
            education.setEducationLevel(request.getEducationLevel());
        }
        if (request.getFieldOfStudy() != null) {
            education.setFieldOfStudy(request.getFieldOfStudy());
        }
        if (request.getDegree() != null) {
            education.setDegree(request.getDegree());
        }
        if (request.getGpa() != null) {
            education.setGpa(request.getGpa());
        }
        if (request.getStartAt() != null) {
            education.setStartAt(request.getStartAt());
        }
        if (request.getEndAt() != null) {
            education.setEndAt(request.getEndAt());
        }
        
        UserEducation saved = userEducationRepository.save(education);
        log.info("Updated education {} for user {}", educationId, userId);
        
        return userEducationMapper.toResponse(saved);
    }

    @Override
    public List<UserEducationResponse> getEducationByUserId(UUID userId) {
        log.info("Fetching education for user: {}", userId);
        
        List<UserEducation> education = userEducationRepository
                .findByUserIdAndIsActiveTrueOrderByStartAtDesc(userId);
        
        return userEducationMapper.toResponseList(education);
    }

    @Override
    public UserEducationResponse getEducationById(UUID userId, UUID educationId) {
        log.info("Fetching education {} for user {}", educationId, userId);
        
        UserEducation education = userEducationRepository.findByIdAndUserIdAndIsActiveTrue(educationId, userId)
                .orElseThrow(() -> new NotFoundException("Education", "id", educationId.toString()));
        
        return userEducationMapper.toResponse(education);
    }

    @Override
    @Transactional
    public void deleteEducation(UUID userId, UUID educationId) {
        log.info("Deleting education {} for user {}", educationId, userId);
        
        UserEducation education = userEducationRepository.findByIdAndUserIdAndIsActiveTrue(educationId, userId)
                .orElseThrow(() -> new NotFoundException("Education", "id", educationId.toString()));
        
        education.deactivate();
        userEducationRepository.save(education);
        
        log.info("Deleted education {} for user {}", educationId, userId);
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", "id", userId.toString());
        }
    }
}

