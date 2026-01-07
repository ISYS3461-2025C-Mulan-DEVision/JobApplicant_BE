// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\common\src\main\java\com\team\ja\common\event\ApplicationCreatedEvent.java

package com.team.ja.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new job application is created.
 * This is a fire-and-forget event (no response expected).
 *
 * Producer: application-service
 * Consumer: notification-service, subscription-service, job-manager-service (and potentially others)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCreatedEvent {

    /**
     * Unique ID for this specific event instance.
     */
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    /**
     * The ID of the job application that was created.
     */
    private UUID applicationId;

    /**
     * The ID of the applicant (user who applied).
     */
    private UUID applicantId;

    /**
     * The ID of the job post being applied to.
     */
    private UUID jobPostId;

    /**
     * The current status of the application (e.g., SUBMITTED, UNDER_REVIEW, etc.).
     */
    private String status;

    /**
     * Timestamp when the application was created.
     */
    private LocalDateTime createdAt;
}
