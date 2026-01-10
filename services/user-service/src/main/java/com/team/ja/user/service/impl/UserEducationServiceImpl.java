package com.team.ja.user.service.impl;

import com.team.ja.common.enumeration.EmploymentType;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;
import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.dto.request.CreateUserEducationRequest;
import com.team.ja.user.dto.request.UpdateUserEducationRequest;
import com.team.ja.user.dto.response.UserEducationResponse;
import com.team.ja.user.mapper.UserEducationMapper;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSearchProfileEmploymentRepository;
import com.team.ja.user.repository.UserSearchProfileJobTitleRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.service.UserEducationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ShardLookupService shardLookupService;
    private final KafkaTemplate<String, UserSearchProfileUpdateEvent> userSearchProfileUpdateKafkaTemplate;

    // For getting Kafka for JM
    private final CountryRepository countryRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserSearchProfileEmploymentRepository userSearchProfileEmploymentRepository;
    private final UserSearchProfileJobTitleRepository userSearchProfileJobTitleRepository;
    private final UserSearchProfileRepository userSearchProfileRepository;

    @Override
    @Transactional
    public UserEducationResponse createEducation(UUID userId, CreateUserEducationRequest request) {
        log.info("Creating education for user: {}", userId);

        // Verify user exists
        validateUserExists(userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
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

            List<UserEducation> educationLevel = userEducationRepository
                    .findByUserIdOrderByEducationLevelRankDesc(userId);

            if (educationLevel.isEmpty()) {
                log.warn("No education records found for user ID: {}", userId);
            } else {
                UserEducation top = educationLevel.get(0);
                String highestEducationLevel = null;
                if (top != null && top.getEducationLevel() != null) {
                    highestEducationLevel = top.getEducationLevel().getDisplayName();
                }
                log.info("Highest education level for user {} is {}", userId, highestEducationLevel);

                Optional<User> user = userRepository.findById(userId);
                String countryAbbreviation = null;
                if (user.isPresent() && user.get().getCountryId() != null) {
                    countryAbbreviation = countryRepository.findById(user.get().getCountryId())
                            .map(c -> c.getAbbreviation())
                            .orElse(null);
                }
                List<UserSkill> allUserSkillIds = userSkillRepository.findByUserIdAndIsActiveTrue(userId);
                Optional<UserSearchProfile> userSearchProfile = userSearchProfileRepository.findByUserId(userId);

                List<EmploymentType> employmentTypes = userSearchProfileEmploymentRepository
                        .findByUserSearchProfileIdAndIsActiveTrue(
                                userSearchProfile.isPresent() ? userSearchProfile.get().getId() : null)
                        .stream()
                        .map(ute -> ute.getEmploymentType())
                        .collect(Collectors.toList());

                List<String> jobTitles = userSearchProfileJobTitleRepository
                        .findByUserSearchProfileIdAndIsActiveTrue(
                                userSearchProfile.isPresent() ? userSearchProfile.get().getId() : null)
                        .stream()
                        .map(utj -> utj.getJobTitle())
                        .collect(Collectors.toList());

                UserSearchProfileUpdateEvent searchProfileEvent = UserSearchProfileUpdateEvent.builder()
                        .userId(userId)
                        .countryAbbreviation(countryAbbreviation)
                        .educationLevel(educationLevel.isEmpty() ? null
                                : educationLevel.get(0).getEducationLevel().name())
                        .employmentTypes(employmentTypes.stream()
                                .map(EmploymentType::name)
                                .collect(Collectors.toList()))
                        .minSalary(userSearchProfile.isPresent() ? userSearchProfile.get().getSalaryMin() : null)
                        .maxSalary(userSearchProfile.isPresent() ? userSearchProfile.get().getSalaryMax() : null)
                        .jobTitles(jobTitles)
                        .skillIds(allUserSkillIds.stream()
                                .map(UserSkill::getId)
                                .collect(Collectors.toList()))
                        .build();
                userSearchProfileUpdateKafkaTemplate.send(KafkaTopics.USER_PROFILE_UPDATE, searchProfileEvent)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Sent UserSearchProfileUpdateEvent for education creation [partition: {}, offset: {}]", 
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset());
                            } else {
                                log.error("Failed to send UserSearchProfileUpdateEvent for education creation", ex);
                            }
                        });
            }

            if (request.getEducationLevel() != null && !educationLevel.isEmpty()) {
                UserEducation highestEducation = educationLevel.get(0);
                log.info("Highest education level for user {} is {}", userId, highestEducation.getEducationLevel());
            }

            return userEducationMapper.toResponse(saved);

        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public UserEducationResponse updateEducation(UUID userId, UUID educationId, UpdateUserEducationRequest request) {
        log.info("Updating education {} for user {}", educationId, userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
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

            List<UserEducation> educationLevel = userEducationRepository
                    .findByUserIdOrderByEducationLevelRankDesc(userId);

            if (educationLevel.isEmpty()) {
                log.warn("No education records found for user ID: {}", userId);
            } else {
                UserEducation top = educationLevel.get(0);
                String highestEducationLevel = null;
                if (top != null && top.getEducationLevel() != null) {
                    highestEducationLevel = top.getEducationLevel().getDisplayName();
                }
                log.info("Highest education level for user {} is {}", userId, highestEducationLevel);

                Optional<User> user = userRepository.findById(userId);
                String countryAbbreviation = null;
                if (user.isPresent() && user.get().getCountryId() != null) {
                    countryAbbreviation = countryRepository.findById(user.get().getCountryId())
                            .map(c -> c.getAbbreviation())
                            .orElse(null);
                }

                Optional<UserSearchProfile> userSearchProfile = userSearchProfileRepository.findByUserId(userId);

                List<EmploymentType> employmentTypes = userSearchProfileEmploymentRepository
                        .findByUserSearchProfileIdAndIsActiveTrue(
                                userSearchProfile.isPresent() ? userSearchProfile.get().getId() : null)
                        .stream()
                        .map(ute -> ute.getEmploymentType())
                        .collect(Collectors.toList());

                List<UserSkill> allUserSkillIds = userSkillRepository.findByUserIdAndIsActiveTrue(userId);

                List<String> jobTitles = userSearchProfileJobTitleRepository
                        .findByUserSearchProfileIdAndIsActiveTrue(
                                userSearchProfile.isPresent() ? userSearchProfile.get().getId() : null)
                        .stream()
                        .map(utj -> utj.getJobTitle())
                        .collect(Collectors.toList());

                UserSearchProfileUpdateEvent searchProfileEvent = UserSearchProfileUpdateEvent.builder()
                        .userId(userId)
                        .countryAbbreviation(countryAbbreviation)
                        .educationLevel(educationLevel.isEmpty() ? null
                                : educationLevel.get(0).getEducationLevel().name())
                        .employmentTypes(employmentTypes.stream()
                                .map(EmploymentType::name)
                                .collect(Collectors.toList()))
                        .minSalary(userSearchProfile.isPresent() ? userSearchProfile.get().getSalaryMin() : null)
                        .maxSalary(userSearchProfile.isPresent() ? userSearchProfile.get().getSalaryMax() : null)
                        .jobTitles(jobTitles)
                        .skillIds(allUserSkillIds.stream()
                                .map(UserSkill::getId)
                                .collect(Collectors.toList()))
                        .build();
                userSearchProfileUpdateKafkaTemplate.send(KafkaTopics.USER_PROFILE_UPDATE, searchProfileEvent)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Sent UserSearchProfileUpdateEvent for education update [partition: {}, offset: {}]", 
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset());
                            } else {
                                log.error("Failed to send UserSearchProfileUpdateEvent for education update", ex);
                            }
                        });
            }

            return userEducationMapper.toResponse(saved);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public List<UserEducationResponse> getEducationByUserId(UUID userId) {
        log.info("Fetching education for user: {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
            List<UserEducation> education = userEducationRepository
                    .findByUserIdAndIsActiveTrueOrderByStartAtDesc(userId);

            return userEducationMapper.toResponseList(education);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public UserEducationResponse getEducationById(UUID userId, UUID educationId) {
        log.info("Fetching education {} for user {}", educationId, userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
            UserEducation education = userEducationRepository.findByIdAndUserIdAndIsActiveTrue(educationId, userId)
                    .orElseThrow(() -> new NotFoundException("Education", "id", educationId.toString()));

            return userEducationMapper.toResponse(education);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public void deleteEducation(UUID userId, UUID educationId) {
        log.info("Deleting education {} for user {}", educationId, userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
            UserEducation education = userEducationRepository.findByIdAndUserIdAndIsActiveTrue(educationId, userId)
                    .orElseThrow(() -> new NotFoundException("Education", "id", educationId.toString()));

            education.deactivate();
            userEducationRepository.save(education);

            log.info("Deleted education {} for user {}", educationId, userId);

            List<UserEducation> educationLevel = userEducationRepository
                    .findByUserIdOrderByEducationLevelRankDesc(userId);

            if (educationLevel.isEmpty()) {
                log.warn("No education records found for user ID: {}", userId);
            } else {
                UserEducation top = educationLevel.get(0);
                String highestEducationLevel = null;
                if (top != null && top.getEducationLevel() != null) {
                    highestEducationLevel = top.getEducationLevel().getDisplayName();
                }
                log.info("Highest education level for user {} is {}", userId, highestEducationLevel);

                Optional<User> user = userRepository.findById(userId);
                String countryAbbreviation = null;
                if (user.isPresent() && user.get().getCountryId() != null) {
                    countryAbbreviation = countryRepository.findById(user.get().getCountryId())
                            .map(c -> c.getAbbreviation())
                            .orElse(null);
                }

                Optional<UserSearchProfile> userSearchProfile = userSearchProfileRepository.findByUserId(userId);

                List<EmploymentType> employmentTypes = userSearchProfileEmploymentRepository
                        .findByUserSearchProfileIdAndIsActiveTrue(
                                userSearchProfile.isPresent() ? userSearchProfile.get().getId() : null)
                        .stream()
                        .map(ute -> ute.getEmploymentType())
                        .collect(Collectors.toList());

                List<UserSkill> allUserSkillIds = userSkillRepository.findByUserIdAndIsActiveTrue(userId);

                List<String> jobTitles = userSearchProfileJobTitleRepository
                        .findByUserSearchProfileIdAndIsActiveTrue(
                                userSearchProfile.isPresent() ? userSearchProfile.get().getId() : null)
                        .stream()
                        .map(utj -> utj.getJobTitle())
                        .collect(Collectors.toList());

                UserSearchProfileUpdateEvent searchProfileEvent = UserSearchProfileUpdateEvent.builder()
                        .userId(userId)
                        .countryAbbreviation(countryAbbreviation)
                        .educationLevel(educationLevel.isEmpty() ? null
                                : educationLevel.get(0).getEducationLevel().name())
                        .employmentTypes(employmentTypes.stream()
                                .map(EmploymentType::name)
                                .collect(Collectors.toList()))
                        .minSalary(userSearchProfile.isPresent() ? userSearchProfile.get().getSalaryMin() : null)
                        .maxSalary(userSearchProfile.isPresent() ? userSearchProfile.get().getSalaryMax() : null)
                        .jobTitles(jobTitles)
                        .skillIds(allUserSkillIds.stream()
                                .map(UserSkill::getId)
                                .collect(Collectors.toList()))
                        .build();
                userSearchProfileUpdateKafkaTemplate.send(KafkaTopics.USER_PROFILE_UPDATE, searchProfileEvent)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Sent UserSearchProfileUpdateEvent for education deletion [partition: {}, offset: {}]", 
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset());
                            } else {
                                log.error("Failed to send UserSearchProfileUpdateEvent for education deletion", ex);
                            }
                        });
            }

        } finally {
            ShardContext.clear();
        }
    }

    private void validateUserExists(UUID userId) {
        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", "id", userId.toString());
        }
    }
}
