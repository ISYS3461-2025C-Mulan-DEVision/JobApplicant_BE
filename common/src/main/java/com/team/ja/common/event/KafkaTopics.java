package com.team.ja.common.event;

/**
 * Kafka topic constants.
 * Centralized topic names to ensure consistency across services.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class
    }

    /**
     * Topic for user registration events.
     * Producer: auth-service
     * Consumer: user-service
     */
    public static final String USER_REGISTERED = "user-registered";

    /**
     * Topic for user profile update events.
     * Producer: user-service
     * Consumer: other services that cache user data
     */
    public static final String USER_UPDATED = "user-updated";

    /**
     * Topic for significant user profile updates (e.g., skills, country).
     * Producer: user-service
     * Consumer: notification-service (for job matching)
     */
    public static final String USER_PROFILE_UPDATED = "user-profile-updated";

    /**
     * Topic for subscription change events.
     * Producer: subscription-service
     * Consumer: user-service (to update premium status)
     */
    public static final String SUBSCRIPTION_CHANGED = "subscription-changed";

    /**
     * Topic for application submitted events.
     * Producer: application-service
     * Consumer: notification-service
     */
    public static final String APPLICATION_SUBMITTED = "application-submitted";
}

