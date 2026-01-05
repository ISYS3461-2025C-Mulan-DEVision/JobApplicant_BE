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
import com.team.ja.user.dto.request.UserWorkExperienceDto;
import com.team.ja.user.model.Country;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserPortfolioItem;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.model.UserWorkExperience;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.SkillRepository;
import com.team.ja.user.repository.UserRepository;

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

        public void migrateUserData(UserMigrationEvent event) {
                log.info("Migrating user data for userId: {} from shard: {} to shard: {}",
                                event.getUserId(), event.getSourceShardId(), event.getTargetShardId());

                try {
                        AtomicReference<String> countryAbbreviationRef = new AtomicReference<>();

                        UserDto migrationDto = loadUserFromSource(event, countryAbbreviationRef);

                        saveUserToTarget(event, migrationDto, countryAbbreviationRef.get());

                        shardLookupService.updateUserShardMapping(event.getUserId(), event.getTargetShardId());

                        cleanUpSourceShard(event.getUserId(), event.getSourceShardId());

                } catch (Exception e) {
                        log.error("Failed to migrate user data for userId: {}: {}",
                                        event.getUserId(), e.getMessage(), e);
                        throw new RuntimeException(e);
                }
        }

        private UserDto loadUserFromSource(UserMigrationEvent event, AtomicReference<String> countryAbbreviationRef) {
                ShardContext.setShardKey(event.getSourceShardId());
                try {
                        return transactionTemplate.execute(status -> {
                                User user = userRepository.findById(event.getUserId())
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "User not found in source shard: "
                                                                                + event.getUserId()));

                                if (event.getNewCountryId() != null) {
                                        countryRepository.findById(event.getNewCountryId())
                                                        .map(Country::getAbbreviation)
                                                        .ifPresent(countryAbbreviationRef::set);
                                }

                                Hibernate.initialize(user.getUserSkills());
                                Hibernate.initialize(user.getEducation());
                                Hibernate.initialize(user.getWorkExperience());
                                Hibernate.initialize(user.getPortfolioItems());

                                return mapToDto(user);
                        });
                } finally {
                        ShardContext.clear();
                }
        }

        private void saveUserToTarget(UserMigrationEvent event, UserDto migrationDto,
                        String targetCountryAbbreviation) {
                ShardContext.setShardKey(event.getTargetShardId());
                try {
                        transactionTemplate.executeWithoutResult(status -> {
                                if (userRepository.existsById(event.getUserId())) {
                                        log.warn("User already exists in target shard: {}, skipping save.",
                                                        event.getTargetShardId());
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

                                UUID localCountryId = null;
                                if (targetCountryAbbreviation != null) {
                                        localCountryId = countryRepository.findAll().stream()
                                                        .filter(c -> c.getAbbreviation()
                                                                        .equalsIgnoreCase(targetCountryAbbreviation))
                                                        .map(Country::getId)
                                                        .findFirst()
                                                        .orElse(null);
                                }

                                if (localCountryId == null) {
                                        localCountryId = event.getNewCountryId();
                                        log.warn("Could not resolve country code '{}' on target shard. Falling back to original ID: {}",
                                                        targetCountryAbbreviation, localCountryId);
                                }

                                targetUser.setCountryId(localCountryId);

                                if (migrationDto.getEducation() != null) {
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
                                }

                                if (migrationDto.getWorkExperience() != null) {
                                        targetUser.setWorkExperience(migrationDto.getWorkExperience().stream()
                                                        .<UserWorkExperience>map(experience -> UserWorkExperience
                                                                        .builder()
                                                                        .id(experience.getId())
                                                                        .user(targetUser)
                                                                        .companyName(experience.getCompanyName())
                                                                        .employmentType(experience.getEmploymentType())
                                                                        .startAt(experience.getStartAt())
                                                                        .endAt(experience.getEndAt())
                                                                        .description(experience.getDescription())
                                                                        .build())
                                                        .toList());
                                }

                                if (migrationDto.getSkills() != null) {
                                        targetUser.setUserSkills(
                                                        migrationDto.getSkills().stream().<UserSkill>map(skill -> {
                                                                Skill targetSkill = skillRepository
                                                                                .findByNameIgnoreCaseAndIsActiveTrue(
                                                                                                skill.getNormalizeName())
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
                                                                                .skillId(targetSkill.getId())
                                                                                .skill(targetSkill)
                                                                                .build();
                                                        }).collect(Collectors.toSet()));
                                }

                                if (migrationDto.getPortfolioItems() != null) {
                                        targetUser.setPortfolioItems(migrationDto.getPortfolioItems().stream()
                                                        .map(item -> UserPortfolioItem.builder()
                                                                        .id(item.getId())
                                                                        .user(targetUser)
                                                                        .fileUrl(item.getFileUrl())
                                                                        .description(item.getDescription())
                                                                        .mediaType(item.getMediaType())
                                                                        .build())
                                                        .collect(Collectors.toSet()));
                                }

                                userRepository.save(targetUser);
                                log.info("Successfully saved user to target shard: {}", event.getTargetShardId());
                        });
                } finally {
                        ShardContext.clear();
                }
        }

        private void cleanUpSourceShard(UUID userId, String sourceShardId) {
                log.info("Cleaning up source shard: {} for user: {}", sourceShardId, userId);
                ShardContext.setShardKey(sourceShardId);
                try {
                        transactionTemplate.executeWithoutResult(status -> {
                                userRepository.deleteById(userId);
                                log.info("Removed user {} from source shard {}", userId, sourceShardId);
                        });
                } catch (Exception e) {
                        log.error("Failed cleanup on source shard {}: {}", sourceShardId, e.getMessage());
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

                if (user.getEducation() != null) {
                        user.getEducation().forEach(e -> dto.getEducation()
                                        .add(com.team.ja.user.dto.request.UserEducationDto.builder()
                                                        .id(e.getId()).institution(e.getInstitution())
                                                        .educationLevel(e.getEducationLevel())
                                                        .fieldOfStudy(e.getFieldOfStudy()).degree(e.getDegree())
                                                        .gpa(e.getGpa())
                                                        .startAt(e.getStartAt()).endAt(e.getEndAt()).build()));
                }

                if (user.getWorkExperience() != null) {
                        user.getWorkExperience().forEach(w -> {
                                String code = "";
                                if (w.getCountryId() != null) {
                                        code = countryRepository.findById(w.getCountryId())
                                                        .map(Country::getAbbreviation)
                                                        .orElse("");
                                }
                                dto.getWorkExperience().add(UserWorkExperienceDto.builder()
                                                .id(w.getId()).jobTitle(w.getJobTitle()).companyName(w.getCompanyName())
                                                .employmentType(w.getEmploymentType()).countryAbbreviation(code)
                                                .startAt(w.getStartAt()).endAt(w.getEndAt()).isCurrent(w.isCurrent())
                                                .description(w.getDescription()).build());
                        });
                }

                if (user.getUserSkills() != null) {
                        user.getUserSkills().forEach(us -> {
                                dto.getSkills().add(new com.team.ja.user.dto.request.UserSkillDto(
                                                us.getSkill().getNormalizedName()));
                        });
                }

                if (user.getPortfolioItems() != null) {
                        user.getPortfolioItems().forEach(p -> dto.getPortfolioItems()
                                        .add(com.team.ja.user.dto.request.UserPortfolioItemDto.builder()
                                                        .id(p.getId()).fileUrl(p.getFileUrl())
                                                        .description(p.getDescription())
                                                        .mediaType(p.getMediaType()).build()));
                }
                return dto;
        }
}