package com.team.ja.user.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.team.ja.common.enumeration.EmploymentType;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.dto.request.CreateSearchProfileJobTitle;
import com.team.ja.user.dto.response.UserSearchProfileJobTitleResponse;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.model.UserSearchProfileJobTitle;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSearchProfileEmploymentRepository;
import com.team.ja.user.repository.UserSearchProfileJobTitleRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.service.UserSearchProfileJobTitleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserSearchProfileJobTitleServiceImpl implements UserSearchProfileJobTitleService {

    private final UserSearchProfileJobTitleRepository userSearchProfileJobTitleRepository;
    private final UserSearchProfileRepository userSearchProfileRepository;
    private final ShardLookupService shardLookupService;
    private final KafkaTemplate<String, UserSearchProfileUpdateEvent> userSearchProfileUpdateKafkaTemplate;

    // For getting Kafka for JM
    private final CountryRepository countryRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final UserSearchProfileEmploymentRepository userSearchProfileEmploymentRepository;
    private final UserEducationRepository userEducationRepository;

    @Override
    @Transactional
    public List<UserSearchProfileJobTitleResponse> createUserSearchProfileJobTitle(CreateSearchProfileJobTitle request,
            UUID searchProfileId) {

        log.info("Creating user search profile job title with request: {} for search profile: {}", request,
                searchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(searchProfileId);
        ShardContext.setShardKey(shardKey);

        try {
            if (userSearchProfileRepository.findById(searchProfileId) == null) {
                log.warn("User search profile does not exist for search profile: {}", searchProfileId);
                throw new IllegalStateException(
                        "User search profile does not exist. Create a profile before adding job titles.");
            }

            List<UserSearchProfileJobTitle> existingJobTitles = userSearchProfileJobTitleRepository
                    .findByUserSearchProfileId(searchProfileId);

            UserSearchProfileJobTitle existingTitle = existingJobTitles.stream()
                    .filter(title -> title.getJobTitle().equalsIgnoreCase(request.getJobTitle()))
                    .findFirst()
                    .orElse(null);

            if (existingTitle == null) {
                UserSearchProfileJobTitle newJobTitle = UserSearchProfileJobTitle.builder()
                        .userSearchProfileId(searchProfileId)
                        .jobTitle(request.getJobTitle())
                        .isActive(true)
                        .build();

                userSearchProfileJobTitleRepository.save(newJobTitle);
            } else if (!existingTitle.isActive()) {
                // Reactivate existing job title
                existingTitle.setActive(true);
                userSearchProfileJobTitleRepository.save(existingTitle);
            } else {
                log.info("Job title '{}' already exists and is active for search profile: {}", request.getJobTitle(),
                        searchProfileId);
            }

            UserSearchProfile userSearchProfile = userSearchProfileRepository.findById(searchProfileId).orElse(null);

            Optional<User> user = userRepository.findFullUserById(userSearchProfile.getUserId());
            String countryAbbreviation = countryRepository.findById(user.get().getCountryId())
                    .map(c -> c.getAbbreviation())
                    .orElse(null);
            List<UserEducation> educationLevel = userEducationRepository
                    .findByUserIdOrderByEducationLevelRankDesc(userSearchProfile.getUserId());

            List<EmploymentType> employmentTypes = userSearchProfileEmploymentRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(searchProfileId)
                    .stream()
                    .map(ute -> ute.getEmploymentType())
                    .collect(Collectors.toList());

            List<String> jobTitles = userSearchProfileJobTitleRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(searchProfileId)
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

            return getUserSearchProfileJobTitles(searchProfileId);
        } finally

        {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public void deleteUserSearchProfileJobTitle(UUID searchProfileId, UUID jobTitleId) {
        log.info("Deleting job title '{}' for search profile: {}", jobTitleId, searchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(searchProfileId);
        ShardContext.setShardKey(shardKey);

        try {
            UserSearchProfileJobTitle titleToDelete = userSearchProfileJobTitleRepository
                    .findByIdAndUserSearchProfileId(jobTitleId, searchProfileId);

            if (titleToDelete == null) {
                throw new IllegalArgumentException(
                        "Job title with ID '" + jobTitleId + "' does not exist for search profile: " + searchProfileId);
            }

            // Soft delete by setting isActive to false
            titleToDelete.setActive(false);
            userSearchProfileJobTitleRepository.save(titleToDelete);

            UserSearchProfile userSearchProfile = userSearchProfileRepository.findById(searchProfileId).orElse(null);

            Optional<User> user = userRepository.findFullUserById(userSearchProfile.getUserId());
            String countryAbbreviation = countryRepository.findById(user.get().getCountryId())
                    .map(c -> c.getAbbreviation())
                    .orElse(null);
            List<UserEducation> educationLevel = userEducationRepository
                    .findByUserIdOrderByEducationLevelRankDesc(userSearchProfile.getUserId());

            List<EmploymentType> employmentTypes = userSearchProfileEmploymentRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(searchProfileId)
                    .stream()
                    .map(ute -> ute.getEmploymentType())
                    .collect(Collectors.toList());

            List<String> jobTitles = userSearchProfileJobTitleRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(searchProfileId)
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

            log.info("Successfully deleted job title '{}' for search profile: {}", titleToDelete.getJobTitle(),
                    searchProfileId);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public List<UserSearchProfileJobTitleResponse> getUserSearchProfileJobTitles(UUID searchProfileId) {
        log.info("Fetching job titles for search profile: {}", searchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(searchProfileId);
        ShardContext.setShardKey(shardKey);

        try {
            List<UserSearchProfileJobTitle> jobTitles = userSearchProfileJobTitleRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(searchProfileId);

            return jobTitles.stream()
                    .map(jobTitle -> UserSearchProfileJobTitleResponse.builder()
                            .jobTitle(jobTitle.getJobTitle())
                            .build())
                    .toList();
        } finally {
            ShardContext.clear();
        }
    }

}
