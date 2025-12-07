package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Authentication providers supported by the system.
 * Used for SSO and manual login tracking.
 */
@Getter
@RequiredArgsConstructor
public enum AuthProvider {

    LOCAL("Local", "Email and password authentication"),
    GOOGLE("Google", "Google OAuth2 authentication"),
    GITHUB("Github", "Github OAuth2 authentication"),
    MICROSOFT("Microsoft", "Microsoft OAuth2 authentication");

    private final String displayName;
    private final String description;
}
