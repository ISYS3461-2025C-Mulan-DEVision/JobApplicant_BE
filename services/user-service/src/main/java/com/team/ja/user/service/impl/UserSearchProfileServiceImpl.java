package com.team.ja.user.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.team.ja.common.enumeration.EducationLevel;
import com.team.ja.common.enumeration.EmploymentType;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.dto.request.CreateSearchProfile;
import com.team.ja.user.dto.request.UpdateSearchProfile;
import com.team.ja.user.dto.response.UserSearchProfileEmploymentResponse;
import com.team.ja.user.dto.response.UserSearchProfileJobTitleResponse;
import com.team.ja.user.dto.response.UserSearchProfileResponse;
import com.team.ja.user.dto.response.UserSearchProfileSkillResponse;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.model.UserSearchProfileEmploymentStatus;
import com.team.ja.user.model.UserSearchProfileJobTitle;
import com.team.ja.user.model.UserSearchProfileSkill;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSearchProfileEmploymentRepository;
import com.team.ja.user.repository.UserSearchProfileJobTitleRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import com.team.ja.user.repository.UserSearchProfileSkillRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.service.UserSearchProfileService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserSearchProfileServiceImpl implements UserSearchProfileService {

    private final UserSearchProfileRepository userSearchProfileRepository;
    private final CountryRepository countryRepository;
    private final ShardLookupService shardLookupService;
    private final UserSearchProfileSkillRepository userSearchProfileSkillRepository;
    private final UserSearchProfileEmploymentRepository userSearchProfileEmploymentRepository;
    private final UserSearchProfileJobTitleRepository userSearchProfileJobTitleRepository;
    private final KafkaTemplate<String, UserSearchProfileUpdateEvent> userSearchProfileUpdateKafkaTemplate;

    // For getting Kafka for JM
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final UserEducationRepository userEducationRepository;

    @Override
    @Transactional
    public UserSearchProfileResponse createUserSearchProfile(CreateSearchProfile request, UUID userId) {

        log.info("Creating user search profile with request: {} for user: {}", request, userId);

        if (userSearchProfileRepository.findByUserId(userId).isPresent()) {
            log.warn("User search profile already exists for user: {}", userId);
            throw new IllegalStateException("User search profile already exists, only one profile allowed per user.");
        }

        if (request.getCountryAbbreviation() != null) {
            countryRepository.findByAbbreviationIgnoreCaseAndIsActiveTrue(request.getCountryAbbreviation()).orElseThrow(
                    () -> new IllegalArgumentException(
                            "Country with abbreviation "
                                    + request.getCountryAbbreviation()
                                    + " does not exist or is inactive."));
        }

        // Get shard key to store the user search profile in the correct database shard
        String shardKey = shardLookupService.findShardIdByUserId(userId);
        log.info("Determined shard key: {} for user ID: {}", shardKey, userId);
        ShardContext.setShardKey(shardKey);

        try {
            UserSearchProfile userSearchProfile = UserSearchProfile.builder()
                    .userId(userId)
                    .salaryMin(request.getSalaryMin())
                    .salaryMax(request.getSalaryMax())
                    .countryAbbreviation(request.getCountryAbbreviation())
                    .educationLevel(request.getEducationLevel())
                    .build();

            UserSearchProfile savedProfile = userSearchProfileRepository.save(userSearchProfile);
            log.info("User search profile saved: {}", savedProfile);

            // TODO: Publish event for search profile creation

        } finally {
            ShardContext.clear();
        }

        return getUserSearchProfileByUserId(userId);

    }

    @Override
    public UserSearchProfileResponse getUserSearchProfileByUserId(UUID userId) {
        log.info("Retrieving user search profile for user ID: {}", userId);

        // Get shard key to retrieve the user search profile from the correct database
        // shard
        String shardKey = shardLookupService.findShardIdByUserId(userId);
        log.info("Determined shard key: {} for user ID: {}", shardKey, userId);
        ShardContext.setShardKey(shardKey);

        try {
            UserSearchProfile profile = userSearchProfileRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("User search profile not found for user ID: " + userId));

            log.info("Retrieved user search profile: {}", profile);
            List<UserSearchProfileSkill> skill = userSearchProfileSkillRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(profile.getId());
            List<UserSearchProfileEmploymentStatus> employment = userSearchProfileEmploymentRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(profile.getId());
            List<UserSearchProfileJobTitle> jobTitles = userSearchProfileJobTitleRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(profile.getId());

            List<UserSearchProfileSkillResponse> skillResponses = skill.stream().map(s -> {
                UserSearchProfileSkillResponse skillResponse = new UserSearchProfileSkillResponse();
                skillResponse.setSkillName(s.getSkill().getNormalizedName());
                return skillResponse;
            }).toList();

            List<UserSearchProfileEmploymentResponse> employmentResponses = employment.stream().map(e -> {
                UserSearchProfileEmploymentResponse employmentResponse = new UserSearchProfileEmploymentResponse();
                employmentResponse.setEmploymentType(e.getEmploymentType());
                return employmentResponse;
            }).toList();

            List<UserSearchProfileJobTitleResponse> jobTitleResponses = jobTitles.stream().map(j -> {
                UserSearchProfileJobTitleResponse jobTitleResponse = new UserSearchProfileJobTitleResponse();
                jobTitleResponse.setJobTitle(j.getJobTitle());
                return jobTitleResponse;
            }).toList();

            UserSearchProfileResponse response = UserSearchProfileResponse.builder()
                    .searchProfileId(profile.getId())
                    .userId(profile.getUserId())
                    .salaryMin(profile.getSalaryMin())
                    .salaryMax(profile.getSalaryMax())
                    .countryAbbreviation(profile.getCountryAbbreviation())
                    .educationLevel(profile.getEducationLevel())
                    .skills(skillResponses)
                    .employments(employmentResponses)
                    .jobTitles(jobTitleResponses)
                    .build();

            return response;
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public UserSearchProfileResponse updateUserSearchProfile(UUID userId, UpdateSearchProfile request) {
        log.info("Updating user search profile for user ID: {} with request: {}", userId, request);

        // Get shard key to update the user search profile in the correct database shard
        String shardKey = shardLookupService.findShardIdByUserId(userId);
        log.info("Determined shard key: {} for user ID: {}", shardKey, userId);
        ShardContext.setShardKey(shardKey);

        try {
            UserSearchProfile existingProfile = userSearchProfileRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("User search profile not found for user ID: " + userId));

            if (request.getCountryAbbreviation() != null) {
                countryRepository.findByAbbreviationIgnoreCaseAndIsActiveTrue(request.getCountryAbbreviation())
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Country with abbreviation "
                                                + request.getCountryAbbreviation()
                                                + " does not exist or is inactive."));
            }

            if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
                if (request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
                    throw new IllegalArgumentException("Minimum salary cannot be greater than maximum salary.");
                }
            }

            if (request.getSalaryMin() != null) {
                existingProfile.setSalaryMin(request.getSalaryMin());

            }
            if (request.getSalaryMax() != null) {
                existingProfile.setSalaryMax(request.getSalaryMax());
            }

            if (request.getCountryAbbreviation() != null) {
                existingProfile.setCountryAbbreviation(request.getCountryAbbreviation());
            }
            if (request.getEducationLevel() != null) {
                existingProfile.setEducationLevel(request.getEducationLevel());
            }

            UserSearchProfile updatedProfile = userSearchProfileRepository.save(existingProfile);
            log.info("Updated user search profile: {}", updatedProfile);

            UserSearchProfile searchProfile = userSearchProfileRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("User search profile not found for user ID: " + userId));

            UserSearchProfile userSearchProfile = userSearchProfileRepository.findById(searchProfile.getId())
                    .orElse(null);

            Optional<User> user = userRepository.findFullUserById(userSearchProfile.getUserId());
            String countryAbbreviation = countryRepository.findById(user.get().getCountryId())
                    .map(c -> c.getAbbreviation())
                    .orElse(null);
            List<UserEducation> educationLevel = userEducationRepository
                    .findByUserIdOrderByEducationLevelRankDesc(userSearchProfile.getUserId());

            List<EmploymentType> employmentTypes = userSearchProfileEmploymentRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(userSearchProfile.getId())
                    .stream()
                    .map(ute -> ute.getEmploymentType())
                    .collect(Collectors.toList());

            List<String> jobTitles = userSearchProfileJobTitleRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(userSearchProfile.getId())
                    .stream()
                    .map(utj -> utj.getJobTitle())
                    .collect(Collectors.toList());

            List<UserSkill> allUserSkillIds = userSkillRepository
                    .findByUserIdAndIsActiveTrue(userSearchProfile.getUserId());

            UserSearchProfileUpdateEvent searchProfileEvent = UserSearchProfileUpdateEvent.builder()
                    .userId(userSearchProfile.getUserId())
                    .countryAbbreviation(countryAbbreviation)
                    .educationLevel(educationLevel.isEmpty() ? null
                            : educationLevel.get(0).getEducationLevel().name())
                    .employmentTypes(employmentTypes.stream()
                            .map(EmploymentType::name)
                            .collect(Collectors.toList()))
                    .minSalary(userSearchProfile.getSalaryMin())
                    .maxSalary(userSearchProfile.getSalaryMax())
                    .jobTitles(jobTitles)
                    .skillIds(allUserSkillIds.stream()
                            .map(UserSkill::getId)
                            .collect(Collectors.toList()))
                    .build();
            userSearchProfileUpdateKafkaTemplate.send(KafkaTopics.USER_PROFILE_UPDATE, searchProfileEvent);
            return getUserSearchProfileByUserId(userId);

        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public void deactivateUserSearchProfile(UUID userId) {
        log.info("Deactivating user search profile for user ID: {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        log.info("Determined shard key: {} for user ID: {}", shardKey, userId);
        ShardContext.setShardKey(shardKey);

        try {
            UserSearchProfile existingProfile = userSearchProfileRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("User search profile not found for user ID: " + userId));

            existingProfile.setActive(false);
            userSearchProfileRepository.save(existingProfile);

            // TODO: Publish event for search profile deactivation notification

            log.info("Deactivated user search profile for user ID: {}", userId);
        } finally {
            ShardContext.clear();
        }
    }

}
