package com.team.ja.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Subscription statuses supported by the system.
 * Used for subscription status tracking.
 */

@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {

    ACTIVE("Active", "Actice Status"),
    EXPIRED("Expired", "Expired Status"),
    CANCELLED("Cancelled", "Cancelled Status"),
    PAST_DUE("Past Due", "Past Due Status"),
    TRIAL("Trial", "Trial Status"),
    PENDING("Pending", "Pending Status");

    private final String displayName;
    private final String description;
}
