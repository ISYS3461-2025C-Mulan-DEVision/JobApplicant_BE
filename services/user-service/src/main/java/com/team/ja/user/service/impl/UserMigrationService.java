package com.team.ja.user.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.team.ja.common.event.UserMigrationEvent;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.config.sharding.ShardingProperties;
import com.team.ja.user.dto.request.UserDto;
import com.team.ja.user.model.Country;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserPortfolioItem;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.model.UserWorkExperience;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.SkillRepository;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserPortfolioItemRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.repository.UserWorkExperienceRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMigrationService {

        private final UserRepository userRepository;
        private final UserEducationRepository userEducationRepository;
        private final UserWorkExperienceRepository userWorkExperienceRepository;
        private final UserPortfolioItemRepository userPortfolioItemRepository;
        private final UserSkillRepository userSkillRepository;
        private final ShardLookupService shardLookupService;
        private final CountryRepository countryRepository;
        private final SkillRepository skillRepository;
        private final EntityManager entityManager;
        private final ShardingProperties shardingProperties;

        public void migrateUserData(UserMigrationEvent event) {

                ShardContext.setShardKey(event.getSourceShardId());
                User user = userRepository.findById(event.getUserId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User not found in source shard: " + event.getUserId()));
                log.info("Migrating user data for userId: {} from shard: {} to shard: {}",
                                event.getUserId(), event.getSourceShardId(), event.getTargetShardId());

                try {
                        UserDto migrationDto = loadUserFromSource(event);

                        saveUserToTarget(event, migrationDto);

                        shardLookupService.updateUserShardMapping(event.getUserId(), event.getTargetShardId());

                        cleanUpSourceShard(event.getUserId(), event.getSourceShardId());

                } catch (Exception e) {
                        log.error("Failed to migrate user data for userId: {}: {}",
                                        event.getUserId(), e.getMessage());
                }

        }

        private void cleanUpSourceShard(UUID userId, String sourceShardId) {
                log.info("Cleaning up source shard: {} for user: {}", sourceShardId, userId);

                ShardContext.setShardKey(sourceShardId);

                try {
                        userRepository.deleteById(userId);

                        log.info("Successfully removed user {} from source shard {}", userId, sourceShardId);
                } catch (Exception e) {
                        log.error("Failed to delete user {} from source shard {}. Manual cleanup may be required.",
                                        userId, sourceShardId, e);
                } finally {
                        ShardContext.clear();
                }
        }

        private UserDto loadUserFromSource(UserMigrationEvent event) {
                ShardContext.setShardKey(event.getSourceShardId());
                try {
                        User user = userRepository.findById(event.getUserId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "User not found in source shard: " + event.getUserId()));

                        user.getUserSkills().size();
                        user.getEducation().size();
                        user.getWorkExperience().size();
                        user.getPortfolioItems().size();

                        return mapToDto(user);

                } finally {
                        ShardContext.clear();
                }
        }

        private void saveUserToTarget(UserMigrationEvent event, UserDto migrationDto) {
                ShardContext.setShardKey(event.getTargetShardId());
                try {
                        if (userRepository.existsById(event.getUserId())) {
                                log.warn("User already exists in target shard: {}, skipping migration for userId: {}",
                                                event.getTargetShardId(), event.getUserId());
                                return;
                        }

                        User targetUser = User.builder()
                                        .id(migrationDto.getId())
                                        .email(migrationDto.getEmail())
                                        .firstName(migrationDto.getFirstName())
                                        .lastName(migrationDto.getLastName())
                                        .phone(migrationDto.getPhone())
                                        .address(migrationDto.getAddress())
                                        .build();

                        targetUser.setCountryId(event.getNewCountryId());

                        targetUser.setEducation(migrationDto.getEducation().stream()
                                        .<UserEducation>map(education -> UserEducation.builder()
                                                        .id(education.getId())
                                                        .userId(targetUser.getId())
                                                        .user(targetUser)
                                                        .institution(education.getInstitution())
                                                        .degree(education.getDegree())
                                                        .gpa(education.getGpa())
                                                        .fieldOfStudy(education.getFieldOfStudy())
                                                        .startAt(education.getStartAt())
                                                        .endAt(education.getEndAt())
                                                        .build())
                                        .toList());

                        targetUser.setWorkExperience(migrationDto.getWorkExperience().stream()
                                        .<UserWorkExperience>map(experience -> UserWorkExperience.builder()
                                                        .id(experience.getId())
                                                        .user(targetUser)
                                                        .companyName(experience.getCompanyName())
                                                        .employmentType(experience.getEmploymentType())
                                                        .startAt(experience.getStartAt())
                                                        .endAt(experience.getEndAt())
                                                        .description(experience.getDescription())
                                                        .build())
                                        .toList());

                        targetUser.setUserSkills(migrationDto.getSkills().stream().<UserSkill>map(skill -> {

                                Skill targetSkill = skillRepository
                                                .findByNameIgnoreCaseAndIsActiveTrue(skill.getNormalizeName())
                                                .orElseGet(() -> {
                                                        Skill newSkill = Skill.builder()
                                                                        .name(skill.getNormalizeName())
                                                                        .build();
                                                        skillRepository.save(newSkill);
                                                        return newSkill;
                                                });
                                return UserSkill.builder()
                                                .userId(targetUser.getId())
                                                .user(targetUser)
                                                .skillId(targetUser.getId())
                                                .skill(targetSkill)
                                                .build();
                        }).collect(Collectors.toSet()));

                        targetUser.setPortfolioItems(migrationDto.getPortfolioItems().stream()
                                        .map(item -> UserPortfolioItem.builder()
                                                        .id(item.getId())
                                                        .user(targetUser)
                                                        .fileUrl(item.getFileUrl())
                                                        .description(item.getDescription())
                                                        .mediaType(item.getMediaType())
                                                        .build())
                                        .collect(Collectors.toSet()));

                        userRepository.save(targetUser);

                        log.info("Successfully migrated user data for userId: {} to shard: {}",
                                        event.getUserId(), event.getTargetShardId());

                } finally {
                        ShardContext.clear();
                }
        }

        private UserDto mapToDto(User user) {
                UserDto dto = UserDto.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .phone(user.getPhone())
                                .address(user.getAddress())
                                .city(user.getCity())
                                .objectiveSummary(user.getObjectiveSummary())
                                .avatarUrl(user.getAvatarUrl())
                                .isPremium(user.isPremium())
                                .education(new ArrayList<>())
                                .workExperience(new ArrayList<>())
                                .portfolioItems(new ArrayList<>())
                                .skills(new ArrayList<>())
                                .build();

                user.getEducation().forEach(e -> dto.getEducation()
                                .add(com.team.ja.user.dto.request.UserEducationDto.builder()
                                                .id(e.getId()).institution(e.getInstitution())
                                                .educationLevel(e.getEducationLevel())
                                                .fieldOfStudy(e.getFieldOfStudy()).degree(e.getDegree()).gpa(e.getGpa())
                                                .startAt(e.getStartAt()).endAt(e.getEndAt()).build()));

                user.getWorkExperience().forEach(w -> {
                        String code = countryRepository.findById(w.getCountryId())
                                        .map(Country::getAbbreviation)
                                        .orElse("UNKNOWN");
                        dto.getWorkExperience().add(com.team.ja.user.dto.request.UserWorkExperienceDto.builder()
                                        .id(w.getId()).jobTitle(w.getJobTitle()).companyName(w.getCompanyName())
                                        .employmentType(w.getEmploymentType()).countryAbbreviation(code)
                                        .startAt(w.getStartAt()).endAt(w.getEndAt()).isCurrent(w.isCurrent())
                                        .description(w.getDescription()).build());
                });

                user.getUserSkills().forEach(us -> {
                        dto.getSkills().add(new com.team.ja.user.dto.request.UserSkillDto(
                                        us.getSkill().getNormalizedName()));
                });

                user.getPortfolioItems()
                                .forEach(p -> dto.getPortfolioItems()
                                                .add(com.team.ja.user.dto.request.UserPortfolioItemDto.builder()
                                                                .id(p.getId()).fileUrl(p.getFileUrl())
                                                                .description(p.getDescription())
                                                                .mediaType(p.getMediaType()).build()));
                return dto;
        }
}
