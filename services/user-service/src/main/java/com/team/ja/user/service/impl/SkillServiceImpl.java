package com.team.ja.user.service.impl;

import com.team.ja.common.exception.ConflictException;
import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.dto.response.SkillResponse;
import com.team.ja.user.mapper.SkillMapper;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.repository.SkillRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of SkillService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillMapper skillMapper;

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

        // Verify user exists
        validateUserExists(userId);

        // Validate all skills exist
        List<Skill> skills = skillRepository.findByIdInAndIsActiveTrue(skillIds);
        if (skills.size() != skillIds.size()) {
            throw new NotFoundException("Some skills not found");
        }

        // Add skills that user doesn't already have
        for (UUID skillId : skillIds) {
            // Check if user currently has this skill active
            if (userSkillRepository.existsByUserIdAndSkillIdAndIsActiveTrue(userId, skillId)) {
                continue;
            }
            // Check if user had this skill but set as inactive
            var inactiveUserSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId);
            if (inactiveUserSkill.isPresent()) {
                var userSkill = inactiveUserSkill.get();
                userSkill.activate();
                userSkillRepository.save(userSkill);

                // Increment usage count
                Skill skill = skills.stream()
                        .filter(s -> s.getId().equals(skillId))
                        .findFirst()
                        .orElseThrow();
                skill.setUsageCount(skill.getUsageCount() + 1);
                skillRepository.save(skill);
            }

            if (!userSkillRepository.existsByUserIdAndSkillIdAndIsActiveTrue(userId, skillId)) {
                UserSkill userSkill = UserSkill.builder()
                        .userId(userId)
                        .skillId(skillId)
                        .build();
                userSkillRepository.save(userSkill);

                // Increment usage count
                Skill skill = skills.stream()
                        .filter(s -> s.getId().equals(skillId))
                        .findFirst()
                        .orElseThrow();
                skill.setUsageCount(skill.getUsageCount() + 1);
                skillRepository.save(skill);
            }
        }

        log.info("Added skills to user {}", userId);
        return getUserSkills(userId);
    }

    @Override
    @Transactional
    public void removeSkillFromUser(UUID userId, UUID skillId) {
        log.info("Removing skill {} from user {}", skillId, userId);

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

        log.info("Removed skill {} from user {}", skillId, userId);
    }

    @Override
    public List<SkillResponse> getUserSkills(UUID userId) {
        log.info("Fetching skills for user: {}", userId);

        List<UserSkill> userSkills = userSkillRepository.findByUserIdAndIsActiveTrue(userId);
        List<UUID> skillIds = userSkills.stream().map(UserSkill::getSkillId).toList();

        if (skillIds.isEmpty()) {
            return List.of();
        }

        List<Skill> skills = skillRepository.findByIdInAndIsActiveTrue(skillIds);
        return skillMapper.toResponseList(skills);
    }

    @Override
    @Transactional
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

        return skillMapper.toResponse(saved);
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User", "id", userId.toString());
        }
    }
}
