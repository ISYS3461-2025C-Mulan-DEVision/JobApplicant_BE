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
@Transactional(readOnly = true)
public class EmploymentServiceImpl implements EmploymentService {

    private final UserSearchProfileRepository userSearchProfileRepository;
    private final UserSearchProfileEmploymentRepository userSearchProfileEmploymentRepository;
    private final ShardLookupService shardLookupService;

    @Override
    public List<UserSearchProfileEmploymentResponse> addEmployment(CreateSearchProfileEmployment event,
            UUID userSearchProfileId) {

        log.info("Adding employment status for user: {}", userSearchProfileId);

        String shardKey = shardLookupService.findShardIdBySearchProfileId(userSearchProfileId);
        ShardContext.setShardKey(shardKey);

        try {

            if (userSearchProfileRepository.findByUserId(userSearchProfileId).isEmpty()) {
                log.error("UserSearchProfile not found for userId: {}", userSearchProfileId);
                throw new IllegalArgumentException("UserSearchProfile not found");
            }

            if (event.getEmploymentType() == null || event.getEmploymentType() != EmploymentType.CONTRACT
                    && event.getEmploymentType() != EmploymentType.FULL_TIME
                    && event.getEmploymentType() != EmploymentType.PART_TIME
                    && event.getEmploymentType() != EmploymentType.FREELANCE
                    && event.getEmploymentType() != EmploymentType.INTERNSHIP) {
                log.error("Invalid employment type provided for userId: {}", userSearchProfileId);
                throw new IllegalArgumentException("Invalid employment type");
            }

            List<UserSearchProfileEmploymentStatus> existingStatuses = userSearchProfileEmploymentRepository
                    .findByUserSearchProfileIdAndIsActiveTrue(userSearchProfileId);

            for (EmploymentType employmentType : EmploymentType.values()) {
                UserSearchProfileEmploymentStatus existingStatus = existingStatuses.stream()
                        .filter(status -> status.getEmploymentType() == employmentType)
                        .findFirst()
                        .orElse(null);

                if (existingStatus == null) {
                    // New employment type
                    UserSearchProfileEmploymentStatus employmentStatus = new UserSearchProfileEmploymentStatus();
                    employmentStatus.setUserSearchProfileId(userSearchProfileId);
                    employmentStatus.setEmploymentType(employmentType);
                    userSearchProfileEmploymentRepository.save(employmentStatus);

                } else if (!existingStatus.isActive()) {

                    // Reactivate existing employment type
                    existingStatus.setActive(true);
                    userSearchProfileEmploymentRepository.save(existingStatus);
                } else {
                    // Already active, do nothing
                    log.info("Employment type {} already active for user: {}", employmentType, userSearchProfileId);
                }

            }

            log.info("Employment status added successfully for user: {}", userSearchProfileId);

            // TODO: Publish event for search profile employment

            return getEmploymentStatusByUserId(userSearchProfileId);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
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
