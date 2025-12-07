package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Education level enumeration with ordering support.
 * Used to categorize user education history.
 */
@Getter
@RequiredArgsConstructor
public enum EducationLevel {

    HIGH_SCHOOL("High School", 1),
    VOCATIONAL("Vocational/Technical", 2),
    ASSOCIATE("Associate Degree", 3),
    BACHELOR("Bachelor's Degree", 4),
    MASTER("Master's Degree", 5),
    DOCTORATE("Doctorate/PhD", 6),
    PROFESSIONAL("Professional Degree", 7);  // MD, JD, MBA, etc.

    private final String displayName;
    private final int levelOrder;

    /**
     * Check if this level is higher than another level.
     */
    public boolean isHigherThan(EducationLevel other) {
        return this.levelOrder > other.levelOrder;
    }

    /**
     * Check if this level is at least the specified level.
     */
    public boolean isAtLeast(EducationLevel minimumLevel) {
        return this.levelOrder >= minimumLevel.levelOrder;
    }
}

