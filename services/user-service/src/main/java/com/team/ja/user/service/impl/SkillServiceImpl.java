package com.team.ja.user.service.impl;

import com.team.ja.common.enumeration.EmploymentType;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.SkillCreateEvent;
import com.team.ja.common.event.UserProfileUpdatedEvent;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;
import com.team.ja.common.exception.ConflictException;
import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.dto.response.SkillResponse;
import com.team.ja.user.dto.response.UserSearchProfileSkillResponse;
import com.team.ja.user.kafka.SkillCreateProducer;
import com.team.ja.user.kafka.UserProfileUpdatedProducer;
import com.team.ja.user.mapper.SkillMapper;
import com.team.ja.user.model.Country;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.model.UserSearchProfileSkill;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.SkillRepository;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSearchProfileEmploymentRepository;
import com.team.ja.user.repository.UserSearchProfileJobTitleRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import com.team.ja.user.repository.UserSearchProfileSkillRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SkillService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    private final UserProfileUpdatedProducer profileUpdatedProducer;
    private final SkillMapper skillMapper;
    private final SkillCreateProducer skillCreateProducer;
    private final UserSearchProfileSkillRepository userSearchProfileSkillRepository;
    private final ShardLookupService shardLookupService;
    private final KafkaTemplate<String, UserSearchProfileUpdateEvent> userSearchProfileUpdateKafkaTemplate;

    // For getting Kafka for JM
    private final CountryRepository countryRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final UserSearchProfileEmploymentRepository userSearchProfileEmploymentRepository;
    private final UserSearchProfileJobTitleRepository userSearchProfileJobTitleRepository;
    private final UserSearchProfileRepository userSearchProfileRepository;
    private final UserEducationRepository userEducationRepository;

    @Override
    public List<SkillResponse> getAllSkills() {
        log.info("Fetching all active skills");
        List<Skill> skills = skillRepository.findByIsActiveTrueOrderByUsageCountDesc();
        return skillMapper.toResponseList(skills);
    }

    @Override
    public List<SkillResponse> getPopularSkills() {
        log.info("Fetching popular skills");
        List<Skill> skills = skillRepository.findTop20ByIsActiveTrueOrderByUsageCountDesc();
        return skillMapper.toResponseList(skills);
    }

    @Override
    public List<SkillResponse> searchSkills(String query) {
        log.info("Searching skills with query: {}", query);
        if (query == null || query.trim().isEmpty()) {
            return getPopularSkills();
        }
        List<Skill> skills = skillRepository.searchByName(query.trim());
        return skillMapper.toResponseList(skills);
    }

    @Override
    public SkillResponse getSkillById(UUID id) {
        log.info("Fetching skill by ID: {}", id);
        Skill skill = skillRepository.findById(id)
                .filter(Skill::isActive)
                .orElseThrow(() -> new NotFoundException("Skill", "id", id.toString()));
        return skillMapper.toResponse(skill);
    }

    @Override
    @Transactional
    public List<SkillResponse> addSkillsToUser(UUID userId, List<UUID> skillIds) {
        log.info("Adding {} skills to user {}", skillIds.size(), userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);
        try {
            validateUserExists(userId);

            List<Skill> skills = skillRepository.findByIdInAndIsActiveTrue(skillIds);
            if (skills.size() != skillIds.size()) {
                throw new NotFoundException("One or more skills not found or are inactive.");
            }

            boolean skillsChanged = false;
            // Find all existing skill relations for this user, active or not
            List<UserSkill> existingUserSkills = userSkillRepository.findByUserId(userId);

            for (Skill skillToAdd : skills) {
                UserSkill existingRelation = existingUserSkills.stream()
                        .filter(us -> us.getSkillId().equals(skillToAdd.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingRelation == null) {
                    // This is a brand new skill for the user
                    skillsChanged = true;
                    UserSkill newUserSkill = UserSkill.builder().userId(userId).skillId(skillToAdd.getId()).build();
                    userSkillRepository.save(newUserSkill);
                    skillToAdd.setUsageCount(skillToAdd.getUsageCount() + 1);
                } else if (!existingRelation.isActive()) {
                    // The user had this skill before, but it was inactive
                    skillsChanged = true;
                    existingRelation.activate();
                    userSkillRepository.save(existingRelation);
                    skillToAdd.setUsageCount(skillToAdd.getUsageCount() + 1);
                }
                // If the relation exists and is already active, do nothing.

            }

            skillRepository.saveAll(skills);

            if (skillsChanged) {
                log.info("Skills changed for user {}. Publishing event.", userId);
                List<UUID> allUserSkillIds = userSkillRepository.findByUserIdAndIsActiveTrue(userId)
                        .stream()
                        .map(UserSkill::getSkillId)
                        .collect(Collectors.toList());

                UserProfileUpdatedEvent event = UserProfileUpdatedEvent.builder()
                        .userId(userId)
                        .updateType(UserProfileUpdatedEvent.UpdateType.SKILLS)
                        .skillIds(allUserSkillIds)
                        .build();
                profileUpdatedProducer.sendProfileUpdatedEvent(event);

                Optional<User> user = userRepository.findById(userId);
                String countryAbbreviation = countryRepository.findById(user.get().getCountryId())
                        .map(c -> c.getAbbreviation())
                        .orElse(null);
                List<UserEducation> educationLevel = userEducationRepository
                        .findByUserIdOrderByEducationLevelRankDesc(userId);

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
                        .skillIds(allUserSkillIds)
                        .build();
                userSearchProfileUpdateKafkaTemplate.send(KafkaTopics.USER_PROFILE_UPDATE, searchProfileEvent);

                userRepository.findById(userId).ifPresent(User::markProfileUpdated);
            }

            log.info("Finished adding skills to user {}", userId);
            return getUserSkills(userId);

        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public List<UserSearchProfileSkillResponse> addSkillToUserSearchProfile(List<UUID> skillIds,
            UUID searchProfileId) {
        log.info("Adding {} skills to user {}", skillIds.size(), searchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(searchProfileId);
        ShardContext.setShardKey(shardKey);

        try {
            validateSearchProfileExists(searchProfileId);

            List<Skill> skills = skillRepository.findByIdInAndIsActiveTrue(skillIds);
            if (skills.size() != skillIds.size()) {
                throw new NotFoundException("One or more skills not found or are inactive.");
            }

            boolean skillsChanged = false;
            // Find all existing skill relations for this user, active or not
            List<UserSearchProfileSkill> existingUserSkills = userSearchProfileSkillRepository
                    .findByUserSearchProfileId(searchProfileId);

            for (Skill skillToAdd : skills) {
                UserSearchProfileSkill existingRelation = existingUserSkills.stream()
                        .filter(us -> us.getSkillId().equals(skillToAdd.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingRelation == null) {
                    // This is a brand new skill for the user
                    skillsChanged = true;
                    UserSearchProfileSkill newUserSkill = UserSearchProfileSkill.builder()
                            .skillId(skillToAdd.getId()).build();
                    userSearchProfileSkillRepository.save(newUserSkill);
                    skillToAdd.setUsageCount(skillToAdd.getUsageCount() + 1);
                } else if (!existingRelation.isActive()) {
                    // The user had this skill before, but it was inactive
                    skillsChanged = true;
                    existingRelation.activate();
                    userSearchProfileSkillRepository.save(existingRelation);
                    skillToAdd.setUsageCount(skillToAdd.getUsageCount() + 1);
                }
                // If the relation exists and is already active, do nothing.
            }

            skillRepository.saveAll(skills);

            if (skillsChanged) {
                log.info("Skills changed for user search profile {}. Publishing event.", searchProfileId);
                List<UUID> allUserSearchProfileSkillIds = userSearchProfileSkillRepository
                        .findByUserSearchProfileIdAndIsActiveTrue(searchProfileId)
                        .stream()
                        .map(UserSearchProfileSkill::getSkillId)
                        .collect(Collectors.toList());

            }

            log.info("Finished adding skills to user search profile {}", searchProfileId);
            return getUserSearchProfileSkills(searchProfileId);
        } finally {
            ShardContext.clear();
        }

    }

    @Override
    @Transactional
    public void removeSkillFromUser(UUID userId, UUID skillId) {
        log.info("Removing skill {} from user {}", skillId, userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {

            UserSkill userSkill = userSkillRepository.findByUserIdAndSkillIdAndIsActiveTrue(userId, skillId)
                    .orElseThrow(() -> new NotFoundException("User does not have this skill"));

            userSkill.deactivate();
            userSkillRepository.save(userSkill);

            // Decrement usage count
            skillRepository.findById(skillId).ifPresent(skill -> {
                if (skill.getUsageCount() > 0) {
                    skill.setUsageCount(skill.getUsageCount() - 1);
                    skillRepository.save(skill);
                }
            });

            log.info("Removed skill {} from user {}. Publishing event.", skillId, userId);
            List<UUID> allUserSkillIds = userSkillRepository.findByUserIdAndIsActiveTrue(userId)
                    .stream()
                    .map(UserSkill::getSkillId)
                    .collect(Collectors.toList());

            UserProfileUpdatedEvent event = UserProfileUpdatedEvent.builder()
                    .userId(userId)
                    .updateType(UserProfileUpdatedEvent.UpdateType.SKILLS)
                    .skillIds(allUserSkillIds)
                    .build();
            profileUpdatedProducer.sendProfileUpdatedEvent(event);

            Optional<User> user = userRepository.findById(userId);
            String countryAbbreviation = countryRepository.findById(user.get().getCountryId())
                    .map(c -> c.getAbbreviation())
                    .orElse(null);
            List<UserEducation> educationLevel = userEducationRepository
                    .findByUserIdOrderByEducationLevelRankDesc(userId);

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
                    .skillIds(allUserSkillIds)
                    .build();
            userSearchProfileUpdateKafkaTemplate.send(KafkaTopics.USER_PROFILE_UPDATE, searchProfileEvent);

            // Mark user profile as updated
            userRepository.findById(userId).ifPresent(User::markProfileUpdated);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public void removeSkillFromUserSearchProfile(UUID skillId, UUID searchProfileId) {
        log.info("Removing skill {} from search profile {}", skillId, searchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(searchProfileId);
        ShardContext.setShardKey(shardKey);

        try {

            UserSearchProfileSkill userSkill = userSearchProfileSkillRepository
                    .findByUserSearchProfileIdAndSkillIdAndIsActiveTrue(searchProfileId, skillId)
                    .orElseThrow(() -> new NotFoundException("User does not have this skill in search profile"));

            userSkill.deactivate();
            userSearchProfileSkillRepository.save(userSkill);

            // Decrement usage count
            skillRepository.findById(skillId).ifPresent(skill -> {
                if (skill.getUsageCount() > 0) {
                    skill.setUsageCount(skill.getUsageCount() - 1);
                    skillRepository.save(skill);
                }
            });
            log.info("Removed skill {} from search profile {}. Publishing event.", skillId, searchProfileId);
            List<UUID> allUserSearchProfileSkillIds = userSearchProfileSkillRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(searchProfileId)
                    .stream()
                    .map(UserSearchProfileSkill::getSkillId)
                    .collect(Collectors.toList());

            // TODO: Add event publishing for search profile skill removal if needed

        } finally {
            ShardContext.clear();
        }

    }

    @Override
    public List<SkillResponse> getUserSkills(UUID userId) {
        log.info("Fetching skills for user: {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {

            List<UserSkill> userSkills = userSkillRepository.findByUserIdAndIsActiveTrue(userId);
            List<UUID> skillIds = userSkills.stream().map(UserSkill::getSkillId).toList();

            if (skillIds.isEmpty()) {
                return List.of();
            }

            List<Skill> skills = skillRepository.findByIdInAndIsActiveTrue(skillIds);
            return skillMapper.toResponseList(skills);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public List<UserSearchProfileSkillResponse> getUserSearchProfileSkills(UUID searchProfileId) {
        log.info("Fetching search profile skills for search profile: {}", searchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(searchProfileId);
        ShardContext.setShardKey(shardKey);

        try {

            List<UserSearchProfileSkill> userSkills = userSearchProfileSkillRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(searchProfileId);
            List<UUID> skillIds = userSkills.stream().map(UserSearchProfileSkill::getSkillId).toList();

            if (skillIds.isEmpty()) {
                return List.of();
            }

            List<Skill> skills = skillRepository.findByIdInAndIsActiveTrue(skillIds);
            return skillMapper.toUserSearchProfileSkillResponseList(skills);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SkillResponse createSkill(String name) {
        log.info("Creating new skill: {}", name);

        String trimmedName = name.trim();
        String normalizedName = trimmedName.toLowerCase();

        // Check if skill already exists
        if (skillRepository.existsByNameIgnoreCaseAndIsActiveTrue(trimmedName)) {
            throw new ConflictException("Skill with name '" + trimmedName + "' already exists");
        }

        Skill skill = Skill.builder()
                .name(trimmedName)
                .normalizedName(normalizedName)
                .usageCount(0)
                .build();

        Skill saved = skillRepository.save(skill);
        log.info("Created skill with ID: {}", saved.getId());

        // Notify other shards about the new skill
        SkillCreateEvent event = SkillCreateEvent.builder()
                .skillId(saved.getId())
                .name(saved.getName())
                .normalizedName(saved.getNormalizedName())
                .build();

        skillCreateProducer.sendSkillCreateEvent(event);

        return skillMapper.toResponse(saved);

    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", "id", userId.toString());
        }
    }

    private void validateSearchProfileExists(UUID searchProfileId) {
        if (!userSearchProfileSkillRepository.existsByUserSearchProfileId(searchProfileId)) {
            throw new NotFoundException("User Search Profile", "id", searchProfileId.toString());
        }
    }
}
