package com.team.ja.user.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.ja.common.enumeration.EmploymentType;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.dto.request.CreateSearchProfileEmployment;
import com.team.ja.user.dto.response.UserSearchProfileEmploymentResponse;
import com.team.ja.user.model.UserSearchProfileEmploymentStatus;
import com.team.ja.user.repository.UserSearchProfileEmploymentRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import com.team.ja.user.service.EmploymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmploymentServiceImpl implements EmploymentService {

    private final UserSearchProfileRepository userSearchProfileRepository;
    private final UserSearchProfileEmploymentRepository userSearchProfileEmploymentRepository;
    private final ShardLookupService shardLookupService;

    @Override
    @Transactional
    public List<UserSearchProfileEmploymentResponse> addEmployment(CreateSearchProfileEmployment event,
            UUID userSearchProfileId) {

        log.info("Adding employment status for user: {} for request {}", userSearchProfileId, event);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(userSearchProfileId);
        ShardContext.setShardKey(shardKey);

        try {

            if (userSearchProfileRepository.findByIdAndIsActiveTrue(userSearchProfileId).isEmpty()) {
                log.error("UserSearchProfile not found for searchProfileId: {}", userSearchProfileId);
                throw new IllegalArgumentException("UserSearchProfile not found");
            }

            List<UserSearchProfileEmploymentStatus> allStatuses = userSearchProfileEmploymentRepository
                    .findByUserSearchProfileId(userSearchProfileId);

            EmploymentType employmentType = event.getEmploymentType();
            UserSearchProfileEmploymentStatus existingStatus = allStatuses.stream()
                    .filter(status -> status.getEmploymentType() == employmentType)
                    .findFirst()
                    .orElse(null);

            if (existingStatus != null) {
                if (!existingStatus.isActive()) {
                    // Reactivate inactive employment type
                    existingStatus.setActive(true);
                    userSearchProfileEmploymentRepository.save(existingStatus);
                    log.info("Reactivated employment type {} for user: {}", employmentType, userSearchProfileId);
                } else {
                    // Already active, do nothing
                    log.info("Employment type {} already active for user: {}", employmentType, userSearchProfileId);
                }
            } else {
                // New employment type - create it
                UserSearchProfileEmploymentStatus employmentStatus = UserSearchProfileEmploymentStatus.builder()
                        .id(UUID.randomUUID())
                        .userSearchProfileId(userSearchProfileId)
                        .employmentType(employmentType)
                        .build();
                userSearchProfileEmploymentRepository.save(employmentStatus);
                log.info("Created new employment type {} for user: {}", employmentType, userSearchProfileId);
            }

            log.info("Employment status added successfully for user: {}", userSearchProfileId);

            // TODO: Publish event for search profile employment

            return getEmploymentStatusByUserId(userSearchProfileId);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public void removeEmploymentFromUserSearchProfile(UUID userSearchProfileId, UUID employmentId) {

        log.info("Removing employment status {} for user: {}", employmentId, userSearchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(userSearchProfileId);
        ShardContext.setShardKey(shardKey);

        try {

            UserSearchProfileEmploymentStatus employmentStatus = userSearchProfileEmploymentRepository
                    .findById(employmentId)
                    .orElseThrow(() -> {
                        log.error("Employment status not found with id: {}", employmentId);
                        return new IllegalArgumentException("Employment status not found");
                    });

            if (!employmentStatus.getUserSearchProfileId().equals(userSearchProfileId)) {
                log.error("Employment status {} does not belong to user: {}", employmentId, userSearchProfileId);
                throw new IllegalArgumentException("Employment status does not belong to user");
            }

            UserSearchProfileEmploymentStatus existingStatus = userSearchProfileEmploymentRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(userSearchProfileId).stream()
                    .filter(status -> status.getId().equals(employmentId))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("Active employment status not found with id: {} for user: {}", employmentId,
                                userSearchProfileId);
                        return new IllegalArgumentException("User does not have this active employment status");
                    });

            existingStatus.deactivate();
            userSearchProfileEmploymentRepository.save(existingStatus);

            log.info("Employment status {} removed successfully for user: {}", employmentId, userSearchProfileId);
        } finally {
            ShardContext.clear();
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchProfileEmploymentResponse> getEmploymentStatusByUserId(UUID userSearchProfileId) {
        log.info("Fetching employment statuses for user: {}", userSearchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(userSearchProfileId);
        ShardContext.setShardKey(shardKey);

        try {

            List<UserSearchProfileEmploymentStatus> employmentStatuses = userSearchProfileEmploymentRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(userSearchProfileId);

            // Map to response DTOs
            return employmentStatuses.stream().map(status -> {
                UserSearchProfileEmploymentResponse response = new UserSearchProfileEmploymentResponse();
                response.setEmploymentType(status.getEmploymentType());
                return response;
            }).toList();
        } finally {
            ShardContext.clear();
        }
    }
}
