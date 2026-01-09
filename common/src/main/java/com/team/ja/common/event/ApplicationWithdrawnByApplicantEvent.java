// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\common\src\main\java\com\team\ja\common\event\ApplicationWithdrawnByApplicantEvent.java

package com.team.ja.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a job applicant withdraws their application.
 * This is a fire-and-forget event (no response expected).
 *
 * Producer: application-service
 * Consumer: notification-service, job-manager-service, subscription-service (and potentially others)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationWithdrawnByApplicantEvent {

    /**
     * Unique ID for this specific event instance.
     */
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    /**
     * The ID of the job application that was withdrawn.
     */
    private UUID applicationId;

    /**
     * The ID of the applicant (user) who withdrew the application.
     */
    private UUID applicantId;

    /**
     * The ID of the job post from which the application was withdrawn.
     */
    private UUID jobPostId;

    /**
     * Timestamp when the application was withdrawn.
     */
    private LocalDateTime withdrawnAt;

    /**
     * Optional reason why the applicant withdrew the application.
     */
    private String reason;
}
