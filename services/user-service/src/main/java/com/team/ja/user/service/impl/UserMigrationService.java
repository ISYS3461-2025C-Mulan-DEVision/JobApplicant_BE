package com.team.ja.user.service.impl;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.team.ja.common.event.UserMigrationEvent;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.dto.request.UserDto;
import com.team.ja.user.dto.request.UserMigrationDto;
import com.team.ja.user.dto.request.UserWorkExperienceDto;
import com.team.ja.user.model.Country;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserPortfolioItem;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.model.UserSearchProfileEmploymentStatus;
import com.team.ja.user.model.UserSearchProfileJobTitle;
import com.team.ja.user.model.UserSearchProfileSkill;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.model.UserWorkExperience;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.SkillRepository;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserPortfolioItemRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSearchProfileEmploymentRepository;
import com.team.ja.user.repository.UserSearchProfileJobTitleRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import com.team.ja.user.repository.UserSearchProfileSkillRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.repository.UserWorkExperienceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMigrationService {

        private final UserRepository userRepository;
        private final ShardLookupService shardLookupService;
        private final CountryRepository countryRepository;
        private final SkillRepository skillRepository;
        private final TransactionTemplate transactionTemplate;
        private final UserEducationRepository educationRepository;
        private final UserWorkExperienceRepository workExperienceRepository;
        private final UserPortfolioItemRepository portfolioItemRepository;
        private final UserSearchProfileRepository searchProfileRepository;
        private final UserSearchProfileSkillRepository searchProfileSkillRepository;
        private final UserSearchProfileJobTitleRepository searchProfileJobTitleRepository;
        private final UserSearchProfileEmploymentRepository searchProfileEmploymentRepository;
        private final UserSkillRepository userSkillRepository;

        public void migrateUserData(UserMigrationEvent event) {
                log.info("Migrating user data for userId: {} from shard: {} to shard: {}",
                                event.getUserId(), event.getSourceShardId(), event.getTargetShardId());

                try {
                        AtomicReference<String> countryAbbreviationRef = new AtomicReference<>();

                        UserMigrationDto migrationDto = loadUserFromSource(event, countryAbbreviationRef);

                        saveUserToTarget(event, migrationDto, countryAbbreviationRef.get());

                        shardLookupService.updateUserShardMapping(event.getUserId(), event.getTargetShardId());

                        cleanUpSourceShard(event.getUserId(), event.getSourceShardId());

                } catch (Exception e) {
                        log.error("Failed to migrate user data for userId: {}: {}",
                                        event.getUserId(), e.getMessage(), e);
                        throw new RuntimeException(e);
                }
        }

        private UserMigrationDto loadUserFromSource(UserMigrationEvent event,
                        AtomicReference<String> countryAbbreviationRef) {
                ShardContext.setShardKey(event.getSourceShardId());
                try {
                        return transactionTemplate.execute(status -> {
                                // Get all data separately - no Hibernate fetch issues
                                UserMigrationDto data = getMigrationData(event.getUserId());

                                if (event != null) {
                                        countryRepository
                                                        .findByAbbreviationIgnoreCaseAndIsActiveTrue(
                                                                        event.getNewCountryAbbreviation())
                                                        .ifPresent(country -> countryAbbreviationRef
                                                                        .set(country.getAbbreviation()));
                                }

                                return data;
                        });
                } finally {
                        ShardContext.clear();
                }
        }

        private UserMigrationDto getMigrationData(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

                UserSearchProfile searchProfile = searchProfileRepository.findByUserId(userId).orElse(null);

                return UserMigrationDto.builder()
                                .user(user)
                                .education(educationRepository.findByUserId(userId))
                                .workExperience(workExperienceRepository.findByUserId(userId))
                                .skills(userSkillRepository.findByUserId(userId))
                                .portfolioItems(portfolioItemRepository.findByUserId(userId))
                                .searchProfile(searchProfile)
                                .searchProfileSkills(searchProfile != null
                                                ? searchProfileSkillRepository
                                                                .findByUserSearchProfileId(searchProfile.getId())
                                                : new ArrayList<>())
                                .searchProfileJobTitles(searchProfile != null
                                                ? searchProfileJobTitleRepository
                                                                .findByUserSearchProfileId(searchProfile.getId())
                                                : new ArrayList<>())
                                .searchProfileEmployments(searchProfile != null
                                                ? searchProfileEmploymentRepository
                                                                .findByUserSearchProfileId(searchProfile.getId())
                                                : new ArrayList<>())
                                .build();
        }

        private void saveUserToTarget(UserMigrationEvent event, UserMigrationDto migrationData,
                        String targetCountryAbbreviation) {
                ShardContext.setShardKey(event.getTargetShardId());
                try {
                        transactionTemplate.executeWithoutResult(status -> {
                                if (userRepository.existsById(event.getUserId())) {
                                        log.warn("User already exists in target shard: {}, skipping save.",
                                                        event.getTargetShardId());
                                        return;
                                }

                                User sourceUser = migrationData.getUser();
                                User targetUser = User.builder()
                                                .id(sourceUser.getId())
                                                .email(sourceUser.getEmail())
                                                .firstName(sourceUser.getFirstName())
                                                .lastName(sourceUser.getLastName())
                                                .phone(sourceUser.getPhone())
                                                .address(sourceUser.getAddress())
                                                .city(sourceUser.getCity())
                                                .objectiveSummary(sourceUser.getObjectiveSummary())
                                                .avatarUrl(sourceUser.getAvatarUrl())
                                                .isPremium(sourceUser.isPremium())
                                                .build();

                                // Set country
                                if (targetCountryAbbreviation != null) {
                                        targetUser.setCountryId(countryRepository
                                                        .findByAbbreviationIgnoreCaseAndIsActiveTrue(
                                                                        targetCountryAbbreviation)
                                                        .map(Country::getId)
                                                        .orElse(null));
                                }

                                // Save education
                                if (migrationData.getEducation() != null && !migrationData.getEducation().isEmpty()) {
                                        targetUser.setEducation(migrationData.getEducation().stream()
                                                        .map(e -> UserEducation.builder()
                                                                        .id(e.getId())
                                                                        .userId(targetUser.getId())
                                                                        .user(targetUser)
                                                                        .institution(e.getInstitution())
                                                                        .degree(e.getDegree())
                                                                        .gpa(e.getGpa())
                                                                        .fieldOfStudy(e.getFieldOfStudy())
                                                                        .startAt(e.getStartAt())
                                                                        .endAt(e.getEndAt())
                                                                        .build())
                                                        .collect(Collectors.toList()));
                                }

                                // Save work experience
                                if (migrationData.getWorkExperience() != null
                                                && !migrationData.getWorkExperience().isEmpty()) {
                                        targetUser.setWorkExperience(migrationData.getWorkExperience().stream()
                                                        .map(w -> UserWorkExperience.builder()
                                                                        .id(w.getId())
                                                                        .user(targetUser)
                                                                        .jobTitle(w.getJobTitle())
                                                                        .companyName(w.getCompanyName())
                                                                        .employmentType(w.getEmploymentType())
                                                                        .countryId(w.getCountryId())
                                                                        .startAt(w.getStartAt())
                                                                        .endAt(w.getEndAt())
                                                                        .isCurrent(w.isCurrent())
                                                                        .description(w.getDescription())
                                                                        .build())
                                                        .collect(Collectors.toList()));
                                }

                                // Save skills
                                if (migrationData.getSkills() != null && !migrationData.getSkills().isEmpty()) {
                                        targetUser.setUserSkills(migrationData.getSkills().stream()
                                                        .map(us -> UserSkill.builder()
                                                                        .userId(targetUser.getId())
                                                                        .user(targetUser)
                                                                        .skillId(us.getSkillId())
                                                                        .skill(us.getSkill())
                                                                        .build())
                                                        .collect(Collectors.toSet()));
                                }

                                // Save portfolio items
                                if (migrationData.getPortfolioItems() != null
                                                && !migrationData.getPortfolioItems().isEmpty()) {
                                        targetUser.setPortfolioItems(migrationData.getPortfolioItems().stream()
                                                        .map(p -> UserPortfolioItem.builder()
                                                                        .id(p.getId())
                                                                        .user(targetUser)
                                                                        .fileUrl(p.getFileUrl())
                                                                        .description(p.getDescription())
                                                                        .mediaType(p.getMediaType())
                                                                        .build())
                                                        .collect(Collectors.toSet()));
                                }

                                userRepository.save(targetUser);

                                // Save search profile
                                if (migrationData.getSearchProfile() != null) {
                                        saveSearchProfileToTarget(targetUser, migrationData);
                                }

                                log.info("Successfully saved user and search profile to target shard: {}",
                                                event.getTargetShardId());
                        });
                } finally {
                        ShardContext.clear();
                }
        }

        private void saveSearchProfileToTarget(User targetUser, UserMigrationDto migrationData) {
                UserSearchProfile sourceSearchProfile = migrationData.getSearchProfile();

                UserSearchProfile targetSearchProfile = UserSearchProfile.builder()
                                .id(sourceSearchProfile.getId())
                                .user(targetUser)
                                .countryAbbreviation(sourceSearchProfile.getCountryAbbreviation())
                                .salaryMin(sourceSearchProfile.getSalaryMin())
                                .salaryMax(sourceSearchProfile.getSalaryMax())
                                .isFresher(sourceSearchProfile.getIsFresher())
                                .educationLevel(sourceSearchProfile.getEducationLevel())
                                .build();

                searchProfileRepository.save(targetSearchProfile);

                // Save search profile skills
                if (migrationData.getSearchProfileSkills() != null
                                && !migrationData.getSearchProfileSkills().isEmpty()) {
                        migrationData.getSearchProfileSkills().forEach(spSkill -> {
                                UserSearchProfileSkill targetSpSkill = UserSearchProfileSkill.builder()
                                                .id(spSkill.getId())
                                                .userSearchProfileId(targetSearchProfile.getId())
                                                .skill(spSkill.getSkill())
                                                .build();
                                searchProfileSkillRepository.save(targetSpSkill);
                        });
                }

                // Save search profile job titles
                if (migrationData.getSearchProfileJobTitles() != null
                                && !migrationData.getSearchProfileJobTitles().isEmpty()) {
                        migrationData.getSearchProfileJobTitles().forEach(spTitle -> {
                                UserSearchProfileJobTitle targetSpTitle = UserSearchProfileJobTitle.builder()
                                                .id(spTitle.getId())
                                                .userSearchProfileId(targetSearchProfile.getId())
                                                .jobTitle(spTitle.getJobTitle())
                                                .build();
                                searchProfileJobTitleRepository.save(targetSpTitle);
                        });
                }

                // Save search profile employment types
                if (migrationData.getSearchProfileEmployments() != null
                                && !migrationData.getSearchProfileEmployments().isEmpty()) {
                        migrationData.getSearchProfileEmployments().forEach(spEmployment -> {
                                UserSearchProfileEmploymentStatus targetSpEmployment = UserSearchProfileEmploymentStatus
                                                .builder()
                                                .id(spEmployment.getId())
                                                .userSearchProfileId(targetSearchProfile.getId())
                                                .employmentType(spEmployment.getEmploymentType())
                                                .build();
                                searchProfileEmploymentRepository.save(targetSpEmployment);
                        });
                }

                log.info("Successfully saved search profile {} for user {}",
                                targetSearchProfile.getId(), targetUser.getId());
        }

        private void cleanUpSourceShard(UUID userId, String sourceShardId) {
                log.info("Cleaning up source shard: {} for user: {}", sourceShardId, userId);
                ShardContext.setShardKey(sourceShardId);
                try {
                        transactionTemplate.executeWithoutResult(status -> {
                                // Delete search profile and related data first
                                UserSearchProfile searchProfile = searchProfileRepository.findByUserId(userId)
                                                .orElse(null);
                                if (searchProfile != null) {
                                        searchProfileRepository.delete(searchProfile);
                                        log.debug("Search profile and related data deleted for user: {}", userId);
                                }

                                // Delete user (cascades delete related entities if configured)
                                userRepository.deleteById(userId);
                                log.info("Removed user {} from source shard {}", userId, sourceShardId);
                        });
                } catch (Exception e) {
                        log.error("Failed cleanup on source shard {}: {}", sourceShardId, e.getMessage());
                        throw new RuntimeException("Cleanup failed", e);
                } finally {
                        ShardContext.clear();
                }
        }
}