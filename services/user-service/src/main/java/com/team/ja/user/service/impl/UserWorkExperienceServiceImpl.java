package com.team.ja.user.service.impl;

import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.dto.request.CreateUserWorkExperienceRequest;
import com.team.ja.user.dto.request.UpdateUserWorkExperienceRequest;
import com.team.ja.user.dto.response.UserWorkExperienceResponse;
import com.team.ja.user.mapper.CountryMapper;
import com.team.ja.user.mapper.UserWorkExperienceMapper;
import com.team.ja.user.model.UserWorkExperience;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserWorkExperienceRepository;
import com.team.ja.user.repository.global.CountryRepository;
import com.team.ja.user.service.UserWorkExperienceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of UserWorkExperienceService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserWorkExperienceServiceImpl implements UserWorkExperienceService {

    private final UserWorkExperienceRepository workExperienceRepository;
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final UserWorkExperienceMapper workExperienceMapper;
    private final CountryMapper countryMapper;
    private final ShardLookupService shardLookupService;

    @Override
    @Transactional
    public UserWorkExperienceResponse createWorkExperience(UUID userId, CreateUserWorkExperienceRequest request) {
        log.info("Creating work experience for user: {}", userId);

        // Verify user exists
        validateUserExists(userId);

        // Save work experience in the correct shard with the user's shard key
        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        // Validate country if provided
        if (request.getCountryId() != null) {
            countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new NotFoundException("Country", "id", request.getCountryId().toString()));
        }

        UserWorkExperience workExp = UserWorkExperience.builder()
                .userId(userId)
                .jobTitle(request.getJobTitle())
                .companyName(request.getCompanyName())
                .employmentType(request.getEmploymentType())
                .countryId(request.getCountryId())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .isCurrent(request.isCurrent())
                .description(request.getDescription())
                .build();

        UserWorkExperience saved = workExperienceRepository.save(workExp);
        log.info("Created work experience {} for user {}", saved.getId(), userId);

        return mapWithCountry(saved);
    }

    @Override
    @Transactional
    public UserWorkExperienceResponse updateWorkExperience(UUID userId, UUID workExpId,
            UpdateUserWorkExperienceRequest request) {
        log.info("Updating work experience {} for user {}", workExpId, userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        UserWorkExperience workExp = workExperienceRepository.findByIdAndUserIdAndIsActiveTrue(workExpId, userId)
                .orElseThrow(() -> new NotFoundException("Work Experience", "id", workExpId.toString()));

        // Update fields if provided
        if (request.getJobTitle() != null) {
            workExp.setJobTitle(request.getJobTitle());
        }
        if (request.getCompanyName() != null) {
            workExp.setCompanyName(request.getCompanyName());
        }
        if (request.getEmploymentType() != null) {
            workExp.setEmploymentType(request.getEmploymentType());
        }
        if (request.getCountryId() != null) {
            countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new NotFoundException("Country", "id", request.getCountryId().toString()));
            workExp.setCountryId(request.getCountryId());
        }
        if (request.getStartAt() != null) {
            workExp.setStartAt(request.getStartAt());
        }
        if (request.getEndAt() != null) {
            workExp.setEndAt(request.getEndAt());
        }
        if (request.getCurrent() != null) {
            workExp.setCurrent(request.getCurrent());
        }
        if (request.getDescription() != null) {
            workExp.setDescription(request.getDescription());
        }

        UserWorkExperience saved = workExperienceRepository.save(workExp);
        log.info("Updated work experience {} for user {}", workExpId, userId);

        return mapWithCountry(saved);
    }

    @Override
    public List<UserWorkExperienceResponse> getWorkExperienceByUserId(UUID userId) {
        log.info("Fetching work experience for user: {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        List<UserWorkExperience> workExperiences = workExperienceRepository
                .findByUserIdAndIsActiveTrueOrderByStartAtDesc(userId);

        return workExperiences.stream()
                .map(this::mapWithCountry)
                .toList();
    }

    @Override
    public UserWorkExperienceResponse getWorkExperienceById(UUID userId, UUID workExpId) {
        log.info("Fetching work experience {} for user {}", workExpId, userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        UserWorkExperience workExp = workExperienceRepository.findByIdAndUserIdAndIsActiveTrue(workExpId, userId)
                .orElseThrow(() -> new NotFoundException("Work Experience", "id", workExpId.toString()));

        return mapWithCountry(workExp);
    }

    @Override
    @Transactional
    public void deleteWorkExperience(UUID userId, UUID workExpId) {
        log.info("Deleting work experience {} for user {}", workExpId, userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        UserWorkExperience workExp = workExperienceRepository.findByIdAndUserIdAndIsActiveTrue(workExpId, userId)
                .orElseThrow(() -> new NotFoundException("Work Experience", "id", workExpId.toString()));

        workExp.deactivate();
        workExperienceRepository.save(workExp);

        log.info("Deleted work experience {} for user {}", workExpId, userId);
    }

    private void validateUserExists(UUID userId) {

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", "id", userId.toString());
        }
    }

    private UserWorkExperienceResponse mapWithCountry(UserWorkExperience workExp) {
        UserWorkExperienceResponse response = workExperienceMapper.toResponse(workExp);

        if (workExp.getCountryId() != null) {
            countryRepository.findById(workExp.getCountryId())
                    .map(countryMapper::toResponse)
                    .ifPresent(response::setCountry);
        }

        return response;
    }
}
