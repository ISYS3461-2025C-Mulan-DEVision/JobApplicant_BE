package com.team.ja.common.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostMatchEvent {

    /**
     * User ID who matched the job post.
     */
    private UUID userId;

    /**
     * Job Post ID that matched the user.
     */
    private String jobPostId;

    /**
     * Timestamp when the match occurred.
     */
    @Builder.Default
    private LocalDateTime matchedAt = LocalDateTime.now();

}
