package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * User roles in the JobApplicant system.
 */
@Getter
@RequiredArgsConstructor
public enum Role {

    ADMIN ("Admin account", "Admin Account, used for admin domain"),
    FREE ("Free account", "Free account, restricted access"),
    PREMIMUM ("Premium account", "Premium Account, paid for full access");

    private final String displayName;
    private final String description;
}
