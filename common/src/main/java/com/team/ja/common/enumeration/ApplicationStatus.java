package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Status of a job application throughout its lifecycle.
 */
@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {

    SUBMITTED("Submitted", "Application has been submitted"),
    REVIEW("Review", "Application is being reviewed by recruiter"),
    INTERVIEW("Interview", "Interview has been scheduled"),
    OFFERED("Offered", "Candidate has been offered the position"),
    REJECTED("Rejected", "Application has been rejected"),
    WITHDRAWN("Withdrawn", "Candidate withdrew their application");
    
    private final String displayName;
    private final String description;
}
