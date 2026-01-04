// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\common\src\main\java\com\team\ja\common\enumeration\DocType.java

package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Document type enumeration for job applications.
 * Defines the types of documents that applicants can upload.
 */
@Getter
@RequiredArgsConstructor
public enum DocType {

    /**
     * Resume/CV document - primary qualification document
     */
    RESUME("Resume", "Applicant's resume or curriculum vitae (CV)"),

    /**
     * Cover letter document - supplementary application document
     */
    COVER_LETTER("Cover Letter", "Applicant's cover letter describing interest and qualifications");

    private final String displayName;
    private final String description;
}
